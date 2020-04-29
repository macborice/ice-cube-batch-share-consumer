package com.iceservices.bsc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class BscApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(BscApplication.class, args);
	}

	@Scheduled(fixedRate = 300000)
	public void checkForTermination() {
		//TODO finish processing and close dbconns and other resources on termination
		log.info("checkForTermination not impl yet");
	}

}
