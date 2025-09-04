package it.alnao.springbootexample.port.service.auth;

/**
 * DTO per le statistiche degli utenti.
 */
public class UserStatistics {
    private long totalUsers;
    private long localUsers;
    private long oauth2Users;
    private long enabledUsers;
    private long disabledUsers;
    private long verifiedEmails;
    private long unverifiedEmails;

    // Constructors
    public UserStatistics() {}

    public UserStatistics(long totalUsers, long localUsers, long oauth2Users, 
                         long enabledUsers, long disabledUsers, 
                         long verifiedEmails, long unverifiedEmails) {
        this.totalUsers = totalUsers;
        this.localUsers = localUsers;
        this.oauth2Users = oauth2Users;
        this.enabledUsers = enabledUsers;
        this.disabledUsers = disabledUsers;
        this.verifiedEmails = verifiedEmails;
        this.unverifiedEmails = unverifiedEmails;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getLocalUsers() {
        return localUsers;
    }

    public void setLocalUsers(long localUsers) {
        this.localUsers = localUsers;
    }

    public long getOauth2Users() {
        return oauth2Users;
    }

    public void setOauth2Users(long oauth2Users) {
        this.oauth2Users = oauth2Users;
    }

    public long getEnabledUsers() {
        return enabledUsers;
    }

    public void setEnabledUsers(long enabledUsers) {
        this.enabledUsers = enabledUsers;
    }

    public long getDisabledUsers() {
        return disabledUsers;
    }

    public void setDisabledUsers(long disabledUsers) {
        this.disabledUsers = disabledUsers;
    }

    public long getVerifiedEmails() {
        return verifiedEmails;
    }

    public void setVerifiedEmails(long verifiedEmails) {
        this.verifiedEmails = verifiedEmails;
    }

    public long getUnverifiedEmails() {
        return unverifiedEmails;
    }

    public void setUnverifiedEmails(long unverifiedEmails) {
        this.unverifiedEmails = unverifiedEmails;
    }

    @Override
    public String toString() {
        return "UserStatistics{" +
                "totalUsers=" + totalUsers +
                ", localUsers=" + localUsers +
                ", oauth2Users=" + oauth2Users +
                ", enabledUsers=" + enabledUsers +
                ", disabledUsers=" + disabledUsers +
                ", verifiedEmails=" + verifiedEmails +
                ", unverifiedEmails=" + unverifiedEmails +
                '}';
    }
}
