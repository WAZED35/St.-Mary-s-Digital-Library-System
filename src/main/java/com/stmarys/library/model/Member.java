package com.stmarys.library.model;

/** Represents a library member, either a student or member of staff. */
public class Member {
    private int memberId;
    private String memberName;
    private String email;
    private String membershipType;

    /** Creates a member object. */
    public Member(int memberId, String memberName, String email, String membershipType) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.email = email;
        this.membershipType = membershipType;
    }

    /** Returns the member identifier. */
    public int getMemberId() { return memberId; }
    /** Returns the member name. */
    public String getMemberName() { return memberName; }
    /** Returns the email address. */
    public String getEmail() { return email; }
    /** Returns the membership type. */
    public String getMembershipType() { return membershipType; }

    /** Updates the member name. */
    public void setMemberName(String memberName) { this.memberName = memberName; }
    /** Updates the email address. */
    public void setEmail(String email) { this.email = email; }
    /** Updates the membership type. */
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    /** Returns a readable member description for console output. */
    @Override
    public String toString() {
        return memberId + " | " + memberName + " | " + email + " | " + membershipType;
    }
}
