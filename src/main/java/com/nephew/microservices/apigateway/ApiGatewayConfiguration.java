package com.nephew.microservices.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {
	
	@Bean
	public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
		/*
		 * This first route will redirect /get url to the provided uri. In the filter I am
		 * adding a custom header. This header will be available in the response. In the
		 * filter I am adding a custom parameter.
		 * 
		 * "args": {"MyParam": "MyValue"},
		 * "headers": {"Myheader": "MyURI", ... },
		 */
		return builder.routes()
				.route(p -> p.path("/get")
				// The below filter adds a custom header and a custom parameter to the /get path. 
				.filters(f -> f.addRequestHeader("MyHeader", "MyURI").addRequestParameter("MyParam", "MyValue"))
				.uri("http://httpbin.org:80"))
				.route(p -> p.path("/currency-exchange/**")
				.uri("lb://currency-exchange")) // This finds the service on eureka -- this way load balancing is performed. 
				.route(p -> p.path("/currency-conversion/**")
				.uri("lb://currency-conversion")) // redirects to currency-conversion service that is registered in eureka. 
				.route(p -> p.path("/currency-conversion-feign/**")
				.uri("lb://currency-conversion"))
				// So lets say I want to redirect a route, below is how I can do that. 
				// http://localhost:8765/currency-conversion-new/from/USD/to/INR/quantity/10
				// The above url will return the same results as the /currency-conversion-feign/** url. 
				.route(p -> p.path("/currency-conversion-new/**")
				.filters(f -> f.rewritePath("/currency-conversion-new/(?<segment>.*)", "/currency-conversion-feign/${segment}")) // rewrites the url path, but it doesn't appear to rewrite url path provided in the browser. It just returns the response. 
				.uri("lb://currency-conversion"))
				.build();
	}
	
}

// test url
// http://localhost:8765/currency-conversion/currency-conversion-feign/from/USD/to/INR/quantity/10