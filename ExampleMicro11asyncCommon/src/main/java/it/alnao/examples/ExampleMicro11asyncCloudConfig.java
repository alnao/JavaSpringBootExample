package it.alnao.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("examplemicro11async") //ExampleMicro11async
@PropertySource("classpath:bootstrap.properties")
public class ExampleMicro11asyncCloudConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private String port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.queue}")
    private String queue;
    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;
    
    
    
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getQueue() {
		return queue;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getRoutingkey() {
		return routingkey;
	}
	public void setRoutingkey(String routingkey) {
		this.routingkey = routingkey;
	}
	@Override
	public String toString() {
		return "ExampleMicro11asyncCloudConfig [host=" + host + ", port=" + port + ", username=" + username
				+ ", password=" + password + ", queue=" + queue + ", exchange=" + exchange + ", routingkey="
				+ routingkey + "]";
	}
    		
}





    
    
    

        