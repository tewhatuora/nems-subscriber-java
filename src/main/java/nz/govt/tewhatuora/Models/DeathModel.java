package nz.govt.tewhatuora.Models;

public class DeathModel {
    private String callbackUrl;
    private String deathDate;

    public DeathModel(String callbackUrl, String deathDate) {
        this.callbackUrl = callbackUrl;
        this.deathDate = deathDate;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }
}
