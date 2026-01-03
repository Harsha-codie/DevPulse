package ai.cropplanner.location;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Pincode master data entity
 * Maps pincodes to village/taluk/district with coordinates
 */
@Entity
@Table(name = "pincode_master")
public class Pincode {
    
    @Id
    private String pincode;
    
    private String village;
    private String taluk;
    private String district;
    private Double latitude;
    private Double longitude;
    
    // Constructors
    public Pincode() {}
    
    public Pincode(String pincode, String village, String taluk, String district, 
                   Double latitude, Double longitude) {
        this.pincode = pincode;
        this.village = village;
        this.taluk = taluk;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    
    public String getTaluk() { return taluk; }
    public void setTaluk(String taluk) { this.taluk = taluk; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    @Override
    public String toString() {
        return village + ", " + taluk + " Taluk, " + district + " District";
    }
}
