package pt.ua.icm.bringme.models;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = -8121361232996384115L;
	private String firstName, lastName, email, password;
	private String phoneNumber;
	private int id;

	/**
	 * @param firstName
	 * @param lastName
	 * @param contact
	 */
	public User(String firstName, String lastName, String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 * @param contact
	 */
	public User(String firstName, String lastName, String email,
			String password, String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param phoneNumber
	 */
	public User(String firstName, String lastName, String email, String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the contact
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param contact
	 *            the contact to set
	 */
	public void setContact(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return the User full name
	 */
	public String getFullName() {
		return getFirstName().concat(" ").concat(getLastName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName
				+ ", email=" + email + ", password=" + password + ", phoneNumber="
				+ phoneNumber + "]";
	}

}
