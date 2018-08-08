package RandomLibrary;

import com.github.javafaker.Faker;


import java.util.Locale;
import java.util.Random;

public class RandLibrary {
    Faker faker = new Faker();
    /**
     * +     * @param
     * +     * @auther : Ankit Patel
     * +     * @lastModifiedBy:
     * +
     */
    // Random First Name
    public String firstName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fname = faker.name().firstName();
        System.out.println("First Name: " + fname);
        return fname;

    }

    // Random Last Name
    public String LastName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String lname = faker.name().lastName();
        System.out.println("Last Name: " + lname);
        return lname;
    }

    // Random Email
    public String eMail() {
        Faker faker = new Faker(new Locale("en-IND"));
        String email = faker.internet().emailAddress();
        System.out.println("Email: " + email);
        return email;

    }

    // Random Full Name
    public String userName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullName = faker.name().fullName();
        System.out.println("User Name: " + fullName);
        return fullName;
    }

    // Random Mobile Number
    public String number() {
        int number =  faker.number().numberBetween(75,98);
        String mobileNumber = faker.number().digits(8);
        String phoneNumber = number + mobileNumber;
        System.out.println("Mobile number: " + phoneNumber);
        return phoneNumber;

    }


    // Random Age
    public String AgeAdult() {
        int age = faker.number().numberBetween(22,40);
        Integer intInstance = new Integer(age);
        String numberAsString = intInstance.toString();
        System.out.println("Age: " + numberAsString);
        return numberAsString;
    }



    //Random Birthday
    public String Birthday() {
        int day = faker.number().numberBetween(01,28);
        int month = faker.number().numberBetween(01,12);
        int year = faker.number().numberBetween(1960,1990);
        String birth = day+"/"+month+"/"+year;
        System.out.println("Birthday : "+ birth);
        return birth;
    }
    //Random DebitCardNo
    public String DebitCardNo() {
        String card = faker.business().creditCardNumber();
        System.out.println("Card No: "+card);
        return card;
    }

    public String ExpiryDate() {
        String expiryDate = faker.business().creditCardExpiry();
        System.out.println("Expiry Date: "+expiryDate);
        return expiryDate;
    }

    public String cvvNo() {
        String cvvNo = faker.number().digits(3);
        System.out.println("CVV No: "+cvvNo);
        return cvvNo;
    }

    public String Country() {
        String country = faker.address().country();
        System.out.println("Country Name: "+country);
        return country;
    }
    public String state() {
        Faker faker = new Faker(new Locale("en-IND"));
        String state = faker.address().state();
        System.out.println("Country Name: "+state);
        return state;
    }

    public String city() {
        Faker faker = new Faker(new Locale("en-IND"));
        String city = faker.address().cityName();
        System.out.println("Country Name: "+city);
        return city;
    }

    public String postcode() {
        Faker faker = new Faker(new Locale("en-IND"));
        String zipcode = faker.address().zipCode();
        System.out.println("Pincode: "+zipcode);
        return zipcode;
    }
    public String street() {
        Faker faker = new Faker(new Locale("en-IND"));
        String Street = faker.address().streetName();
        System.out.println("Street: "+Street);
        return Street;
    }
    public String emoji() {
        String emoji = faker.slackEmoji().emoji();
        System.out.println("emoji: "+emoji);
        return emoji;
    }
    public String lorem() {
        String lorem = faker.lorem().sentence();
        System.out.println("lorem: "+lorem);
        return lorem;
    }
    public String maritalStatus() {
        String status = faker.demographic().maritalStatus();
        System.out.println("maritalStatus: "+status);
        return status;
    }
    public String gender() {
        String sex = faker.demographic().sex();
        System.out.println("gender: "+sex);
        return sex;
    }
    public String fullAddress() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullAddress = faker.address().fullAddress();
        System.out.println("fullAddress: "+fullAddress);
        return fullAddress;
    }
    public String RandomNo(int length) {
        String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random no : "+saltStr);
        return saltStr;

    }

    public String RandomNoAlpha(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random no Alpha : "+saltStr);
        return saltStr;
    }

    public String RandomAlpha(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random alpha : "+saltStr);
return saltStr;
    }


}

