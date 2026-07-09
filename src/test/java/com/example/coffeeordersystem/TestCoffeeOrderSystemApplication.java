package com.example.coffeeordersystem;

import org.springframework.boot.SpringApplication;

public class TestCoffeeOrderSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(CoffeeOrderSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
