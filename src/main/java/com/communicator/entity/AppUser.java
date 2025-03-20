package com.communicator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User class to map database connections.
 */
@Getter
@Setter
@NoArgsConstructor
@Document("users")
public class AppUser implements UserDetails {

    @Id
    @Field("phone_number")
    @Pattern(regexp = "^(\\d{1,3})[-.\\s]?(\\d{1,4})[-.\\s]?(\\d{1,4})[-.\\s]?(\\d{1,9})$",
            message = "Please, provide valid phone number.")
    private String phoneNumber;

    @Size(min = 1, message = "First name can't be empty")
    @Field("first_name")
    private String firstName;

    @Size(min = 1, message = "First name can't be empty")
    @Field("last_name")
    private String lastName;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Please, provide valid email format.")
    @Indexed(unique = true)
    private String email;

    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "Password not valid")
    private String password;

    private boolean validated;

    private String verificationCode;
    private String passwordResetCode;

    private String photo = "https://img.freepik.com/premium-vector/illustration-persons-face-outline-icon-symbolizing"
            + "-anonymity_1171540-20820.jpg";

    private boolean active = false;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{'phoneNumber': ?#{#self.phoneNumber}}")
    @JsonIgnore
    private List<Token> tokens = new ArrayList<>();

    private List<String> chatIds = new ArrayList<>();


    /**
     * Tokens getter.
     * @return lists of tokens
     */
    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }

    /**
     * Tokens setter.
     * @param tokensList
     */
    public void setTokens(final List<Token> tokensList) {
        this.tokens = new ArrayList<>(tokensList);
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns true because it's not used.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns true because it's not used.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns true because it's not used.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns true because it's not used.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns true because it's not used.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Method implemented by UsedDetails interface.
     * @return returns empty string because custom User has no username property.
     */
    @Override
    public String getUsername() {
        return this.getEmail();
    }
}
