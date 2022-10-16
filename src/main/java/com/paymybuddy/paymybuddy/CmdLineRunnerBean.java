package com.paymybuddy.paymybuddy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Cmd Line runner to test mvn cycles
 * @author Olivier MOREL
 *
 */
@Component
public class CmdLineRunnerBean {
	@Bean
	public CommandLineRunner runMethod() {
		return args -> System.out.println("run"); 
	}
}
