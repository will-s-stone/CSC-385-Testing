package oz.rest.models;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.Email;
// import org.bson.codecs.pojo.annotations.BsonProperty;
import jakarta.validation.constraints.NotEmpty;

public class Shelter extends AbstractModel {
    // SRS says name as primary key, but should probably be email
    @NotEmpty(message = "Shelter name must not be empty")
    // can't figure out how to make unique fields....
    // @Column(name = "name", unique = true)
    private String name;

    @NotEmpty(message = "Password must not be left empty")
    @Schema(writeOnly = true)
    private String password;

    // TODO: Implement location
    // private String location;

    // @BsonProperty("available_pet_ids")
    // private Set<String> availablePetIds;

    // TODO: idk how to insert the regex that is in the SRS
    // @Pattern(regexp = "")
    // @BsonProperty("phone_number")
    // private String phoneNumber;

    private String image;

    private String description;

    @Email
    private String emailAddress;

    private String phoneNumber;

    private Location location;

    private String latitude;
    private String longitude;

    // public String getUsername() {
    // return username;
    // }

    // public void setUsername(String username) {
    // this.username = username;
    // }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // public Set<String> getAvailablePetIds() {
    // return availablePetIds;
    // }

    // public void setAvailablePetIds(Set<String> availablePetIds) {
    // this.availablePetIds = availablePetIds;
    // }

    // public void addAvailablePetId(String newPetIds) {
    // this.availablePetIds.add(newPetIds);
    // }

    // public void removeAvailablePetId(String toRemove) {
    // this.availablePetIds.remove(toRemove);
    // }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // likely not needed, i think we always add pets one at a time rather than in
    // bulk

    // public void addAvailablePetIds(ArrayList<String> newPetIds) {
    // this.availablePetIds.addAll(newPetIds);
    // }

    // public void removeAvailablePetIds(ArrayList<String> toRemove) {
    // this.availablePetIds.removeAll(toRemove);
    // }
}
