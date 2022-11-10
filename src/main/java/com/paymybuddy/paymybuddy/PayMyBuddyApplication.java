package com.paymybuddy.paymybuddy;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * Main
 * Pour des raisons discutables, Spring Data JPA choisit d’activer par défaut les transactions sur les méthodes des
 * repositories. Cela signifie que les transactions fonctionnent par défaut mais qu’elles sont validées par les méthodes
 * des repositories, c’est-à-dire dans la couche d’accès aux données. Cela peut entraîner des incohérences de données.
 * Par exemple, la méthode d’un service appelle plusieurs méthodes de repositories pour réaliser une fonctionnalité.
 * Si un problème survient au cours de l’exécution de la méthode de service alors les appels déjà effectués aux repositories
 * ne pourront pas être annulés.
 * 
 * Cela signifie qu’un appel à une méthode de repository qui effectue une modification
 * sur la base de données devra être appelée dans le cadre d’une transaction.
 * @author Olivier MOREL
 *
 */

@SpringBootApplication
@PropertySource({"file:./db.properties"})
public class PayMyBuddyApplication {

	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(PayMyBuddyApplication.class, args);
	}

}
