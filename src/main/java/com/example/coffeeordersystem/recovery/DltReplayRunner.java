// 명시적 local 실행에서만 DLT 선택 재발행을 시작하고 종료합니다.
package com.example.coffeeordersystem.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dlt.replay.enabled", havingValue = "true")
public class DltReplayRunner implements ApplicationRunner {

	private final DltReplayService service;
	private final ConfigurableApplicationContext context;

	@Override
	public void run(ApplicationArguments args) {
		DltReplayResult result = service.replay(new DltReplayRequest(
				DltReplayService.DLT_TOPIC,
				Integer.parseInt(required(args, "dlt.replay.partition")),
				Long.parseLong(required(args, "dlt.replay.offset")),
				required(args, "dlt.replay.approved-by"),
				required(args, "dlt.replay.reason")));
		log.info("dlt_replay_result status={} eventId={} approvedBy={} reason={} risk={}",
				result.status(), result.eventId(), required(args, "dlt.replay.approved-by"),
				required(args, "dlt.replay.reason"), result.risk());
		SpringApplication.exit(context, () -> 0);
	}

	private String required(ApplicationArguments args, String name) {
		java.util.List<String> values = args.getOptionValues(name);
		if (values == null || values.isEmpty() || values.get(0).isBlank()) {
			throw new DltReplayException(name + "은 필수입니다.");
		}
		return values.get(0);
	}
}
