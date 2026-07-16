// 통합 테스트 컨텍스트에서 scheduled task를 등록만 하고 실행하지 않는 설정입니다.
package com.example.coffeeordersystem;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

@TestConfiguration(proxyBeanMethods = false)
public class DisabledTaskSchedulerConfiguration {

	@Bean(name = "taskScheduler")
	TaskScheduler taskScheduler() {
		return new DisabledTaskScheduler();
	}
}
