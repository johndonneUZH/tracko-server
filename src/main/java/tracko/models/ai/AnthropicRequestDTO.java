package tracko.models.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnthropicRequestDTO {
    private String model;
    
    @JsonProperty("max_tokens")  // Make sure this annotation is present
    private Integer maxTokens;
    private Double temperature;
    private List<MessageDTO> messages;

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public List<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages;
    }
}