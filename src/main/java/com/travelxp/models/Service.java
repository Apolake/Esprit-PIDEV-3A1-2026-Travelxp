package com.travelxp.models;

public class Service {

    private int serviceId;
    private String providerName;
    private String serviceType;
    private double price;
    private boolean ecoFriendly;
    private int xpReward;

    public Service() {}

    public Service(String providerName, String serviceType, double price, boolean ecoFriendly, int xpReward) {
        this.providerName = providerName;
        this.serviceType = serviceType;
        this.price = price;
        this.ecoFriendly = ecoFriendly;
        this.xpReward = xpReward;
    }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public boolean isEcoFriendly() { return ecoFriendly; }
    public void setEcoFriendly(boolean ecoFriendly) { this.ecoFriendly = ecoFriendly; }
    
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
}
