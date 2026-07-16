// 정상 ranking event의 MySQL ledger 상태와 Redis 적용을 함께 검증합니다.
package com.example.coffeeordersystem.ranking.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.example.coffeeordersystem.TestcontainersConfiguration;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class RankingEventLedgerIntegrationTest {

	@Autowired RankingEventProcessor processor;
	@Autowired ProcessedEventRepository processedEvents;
	@Autowired JdbcTemplate jdbc;
	@Autowired StringRedisTemplate redis;
	@MockitoSpyBean RankingEventLedger rankingEventLedger;

	@BeforeEach
	void setUp() {
		reset(rankingEventLedger);
		processedEvents.deleteAll();
		jdbc.update("delete from ranking_event_ledger");
		redis.delete(redis.keys("popular:menus:*"));
		redis.delete(redis.keys("ranking:applied-event:*"));
	}

	@Test
	void normalConsumerCommitsLedgerAndCompatibilityHistoryAfterRedisApply() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);

		processor.process(event);

		Map<String, Object> ledger = jdbc.queryForMap(
				"select event_type, payload_fingerprint, state, source, rebuild_run_id, "
						+ "reserved_at, redis_applied_at, committed_at "
						+ "from ranking_event_ledger where event_id = ?",
				event.eventId().toString());
		assertThat(ledger)
				.containsEntry("event_type", "order.completed")
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "NORMAL_CONSUMER")
				.containsEntry("rebuild_run_id", null);
		assertThat(ledger.get("payload_fingerprint").toString()).hasSize(64);
		assertThat(ledger.get("reserved_at")).isNotNull();
		assertThat(ledger.get("redis_applied_at")).isNotNull();
		assertThat(ledger.get("committed_at")).isNotNull();
		assertThat(redis.opsForValue().get("ranking:applied-event:" + event.eventId()))
				.isEqualTo(ledger.get("payload_fingerprint"));
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isEqualTo(1.0);
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isTrue();
	}

	@Test
	void redisAppliedStateSurvivesCommitFailureAndRetryDoesNotIncrementTwice() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		doThrow(new IllegalStateException("forced commit transition failure"))
				.when(rankingEventLedger).markCommitted(event.eventId().toString());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("forced commit transition failure");
		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?",
				String.class, event.eventId().toString()))
				.isEqualTo("REDIS_APPLIED");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isEqualTo(1.0);
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isFalse();

		reset(rankingEventLedger);
		processor.process(event);

		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?",
				String.class, event.eventId().toString()))
				.isEqualTo("COMMITTED");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isEqualTo(1.0);
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isTrue();
	}

	@Test
	void redisWrongTypeFailureLeavesNoMarkerAndRetryAppliesScoreOnce() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		String rankingKey = "popular:menus:2026-07-11";
		String markerKey = "ranking:applied-event:" + event.eventId();
		redis.opsForValue().set(rankingKey, "wrong-type");

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("WRONGTYPE");
		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?",
				String.class, event.eventId().toString()))
				.isEqualTo("RESERVED");
		assertThat(redis.opsForValue().get(markerKey)).isNull();
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isFalse();

		redis.delete(rankingKey);
		processor.process(event);

		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?",
				String.class, event.eventId().toString()))
				.isEqualTo("COMMITTED");
		assertThat(redis.opsForValue().get(markerKey)).isEqualTo(RankingEventFingerprint.from(event));
		assertThat(redis.opsForZSet().score(rankingKey, "11")).isEqualTo(1.0);
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isTrue();
	}

	@Test
	void rebuildCommittedEventIsNormalConsumerNoOpAndKeepsOriginalLedgerSource() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		String runId = UUID.randomUUID().toString();
		jdbc.update("insert into ranking_event_ledger(event_id, event_type, payload_fingerprint, state, source, "
					+ "rebuild_run_id, reserved_at, committed_at) values (?,?,?,?,?,?,now(6),now(6))",
				event.eventId().toString(), "order.completed", RankingEventFingerprint.from(event),
				"COMMITTED", "REBUILD", runId);

		processor.process(event);

		Map<String, Object> ledger = jdbc.queryForMap(
				"select state, source, rebuild_run_id from ranking_event_ledger where event_id = ?",
				event.eventId().toString());
		assertThat(ledger)
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "REBUILD")
				.containsEntry("rebuild_run_id", runId);
		assertThat(redis.opsForValue().get("ranking:applied-event:" + event.eventId())).isNull();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isNull();
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isTrue();
	}

	@Test
	void dltSourceWithoutDurableReplayReservationFailsClosed() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);

		assertThatThrownBy(() -> processor.process(event, RankingEventSource.DLT_REPLAY))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("DLT_SOURCE_NOT_RESERVED");

		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isZero();
		assertThat(redis.opsForValue().get("ranking:applied-event:" + event.eventId())).isNull();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isNull();
		assertThat(processedEvents.existsByEventId(event.eventId().toString())).isFalse();
	}

	@Test
	void normalConsumerDifferentFingerprintFailsClosedWithoutChangingLedgerOrRedis() {
		OrderCompletedEvent original = event(UUID.randomUUID(), 11L);
		processor.process(original);
		String marker = redis.opsForValue().get("ranking:applied-event:" + original.eventId());

		OrderCompletedEvent conflicting = event(original.eventId(), 12L);
		assertThatThrownBy(() -> processor.process(conflicting))
				.isInstanceOf(RankingEventPayloadConflictException.class)
				.hasMessageContaining("EVENT_ID_PAYLOAD_CONFLICT");

		assertThat(jdbc.queryForMap(
				"select state, source, payload_fingerprint from ranking_event_ledger where event_id = ?",
				original.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "NORMAL_CONSUMER")
				.containsEntry("payload_fingerprint", marker);
		assertThat(redis.opsForValue().get("ranking:applied-event:" + original.eventId())).isEqualTo(marker);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "11")).isEqualTo(1.0);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-11", "12")).isNull();
	}

	private OrderCompletedEvent event(UUID eventId, Long menuId) {
		return new OrderCompletedEvent(
				eventId, 1L, 2L, menuId, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));
	}
}
