package ai.cropplanner.seed;

public class Dealer {
    private Long id;
    private String name;
    private String city;
    private String district;
    private String contact;
    private String address;
    private String[] availableSeeds;
    private java.util.Map<String, String> seedPrices;
    private Double latitude;
    private Double longitude;
    
    public Dealer() {}
    
    public Dealer(Long id, String name, String city, String district, String contact, 
                 String address, String[] availableSeeds) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.district = district;
        this.contact = contact;
        this.address = address;
        this.availableSeeds = availableSeeds;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String[] getAvailableSeeds() { return availableSeeds; }
    public void setAvailableSeeds(String[] availableSeeds) { this.availableSeeds = availableSeeds; }
    
    public java.util.Map<String, String> getSeedPrices() { return seedPrices; }
    public void setSeedPrices(java.util.Map<String, String> seedPrices) { this.seedPrices = seedPrices; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
