package ai.cropplanner.location;

/**
 * Complete location details with all hierarchical levels
 */
public class LocationDetails {
    
    private String village;
    private String taluk;
    private String district;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String source; // "PINCODE", "MANUAL", "COORDINATES"
    private String message; // Helpful message (e.g., "Auto-detected from pincode")
    
    // Constructors
    public LocationDetails() {}
    
    public LocationDetails(String village, String taluk, String district, 
                          String pincode, Double latitude, Double longitude, String source) {
        this.village = village;
        this.taluk = taluk;
        this.district = district;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.source = source;
    }
    
    // Static factory methods
    public static LocationDetails fromPincode(Pincode pincode) {
        LocationDetails details = new LocationDetails();
        details.village = pincode.getVillage();
        details.taluk = pincode.getTaluk();
        details.district = pincode.getDistrict();
        details.pincode = pincode.getPincode();
        details.latitude = pincode.getLatitude();
        details.longitude = pincode.getLongitude();
        details.source = "PINCODE";
        details.message = "Auto-detected from pincode " + pincode.getPincode();
        return details;
    }
    
    public static LocationDetails fromManual(String village, String taluk, String district, 
                                            Double latitude, Double longitude) {
        LocationDetails details = new LocationDetails();
        details.village = village;
        details.taluk = taluk;
        details.district = district;
        details.latitude = latitude;
        details.longitude = longitude;
        details.source = "MANUAL";
        details.message = "Manually entered location";
        return details;
    }
    
    public static LocationDetails fromCoordinates(Double latitude, Double longitude, 
                                                  String village, String taluk, String district) {
        LocationDetails details = new LocationDetails();
        details.village = village;
        details.taluk = taluk;
        details.district = district;
        details.latitude = latitude;
        details.longitude = longitude;
        details.source = "COORDINATES";
        details.message = "Reverse geocoded from coordinates";
        return details;
    }
    
    // Validation
    public boolean isComplete() {
        return district != null && !district.trim().isEmpty() &&
               latitude != null && longitude != null;
    }
    
    // Display format
    public String getFullAddress() {
        StringBuilder addr = new StringBuilder();
        if (village != null && !village.isEmpty()) {
            addr.append(village).append(", ");
        }
        if (taluk != null && !taluk.isEmpty()) {
            addr.append(taluk).append(" Taluk, ");
        }
        addr.append(district).append(" District");
        if (pincode != null && !pincode.isEmpty()) {
            addr.append(" - ").append(pincode);
        }
        return addr.toString();
    }
    
    // Getters and Setters
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    
    public String getTaluk() { return taluk; }
    public void setTaluk(String taluk) { this.taluk = taluk; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
