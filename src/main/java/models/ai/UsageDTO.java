package ch.uzh.ifi.hase.soprafs24.models.ai;

public class UsageDTO {
    private int inputTokens;
    private int outputTokens;

    // Getters and setters
    public int getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }
}