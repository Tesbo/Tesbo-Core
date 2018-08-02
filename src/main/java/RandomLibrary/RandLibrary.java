package RandomLibrary;

import ExtCode.ExtCode;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.NaturalGenerator;
import me.xdrop.jrand.generators.basics.StringGenerator;
import me.xdrop.jrand.generators.location.CityGenerator;
import me.xdrop.jrand.generators.location.CountryGenerator;
import me.xdrop.jrand.generators.location.PostcodeGenerator;
import me.xdrop.jrand.generators.location.StreetGenerator;
import me.xdrop.jrand.generators.money.CVVGenerator;
import me.xdrop.jrand.generators.money.CardNumberGenerator;
import me.xdrop.jrand.generators.person.*;



import java.util.Random;

public class RandLibrary {
    /**
     * +     * @param
     * +     * @auther : Ankit Patel
     * +     * @lastModifiedBy:
     * +
     */
    // Random First Name
    public String firstName() {
        FirstnameGenerator firstName = JRand.firstname();
        String fName = firstName.gen();
        System.out.println("First Name: " + fName);
        return fName;

    }

    // Random Last Name
    public String LastName() {
        LastnameGenerator lastName = JRand.lastname();
        String name = lastName.gen();
        System.out.println("Last Name: " + name);
        return name;
    }

    // Random Email
    public String eMail() {
        FirstnameGenerator firstName = JRand.firstname();
        String fName = firstName.gen();
        LastnameGenerator lastname = JRand.lastname();
        String lMame = lastname.gen();
        String email = fName + lMame + "@testdata.com";
        System.out.println("Email: " + email);
        return email;

    }

    // Random User Name
    public String userName() {
        NameGenerator name = JRand.name();
        String userName = name.gen();
        System.out.println("User Name: " + userName);
        return userName;
    }

    // Random Indian Mobile Number
    public String number() {
        StringGenerator string = JRand.string();
        String qa = string.range(8, 8).digits().gen();
        NaturalGenerator nat = JRand.natural();
        int as = nat.range(90, 99).gen();
        String to = Integer.toString(as);
        String number = to + qa;
        System.out.println("Mobile number: " + number);
        return number;

    }


    // Random Age Adult
    public String AgeAdult() {
        AgeGenerator age = JRand.age();
        int AgeAdult = age.adult().gen();
        String ageAdultString = Integer.toString(AgeAdult);
        System.out.println("Age Adult: " + ageAdultString);
        return ageAdultString;
    }


    //Random Gender
    public String Gender() {
        GenderGenerator gender = new GenderGenerator();
        String Gender = gender.full().gen();
        System.out.println("Gender: " + Gender);
        return Gender;
    }
    //Random Birthday
    public String Birthday() {
        NaturalGenerator nat = JRand.natural();
        int date = nat.range(01,31).gen();
        int month = nat.range(1,12).gen();
        String Month = Integer.toString(month);
        int year = nat.range(1983,1999).gen();
        String Year = Integer.toString(year);
        String number = ("Birth Date: " + date+"/"+Month+"/"+Year);
        System.out.println(number);
        return number;
    }
    //Random DebitCardNo
    public String DebitCardNo() {
        CardNumberGenerator cardNo = JRand.cardNo();
        String card = cardNo.format("XXXX XXXX XXXX XXXX").gen();
        System.out.println("Card No: "+card);
        return card;
    }

    public String ExpiryDate() {
        NaturalGenerator nat = JRand.natural();
        int date = nat.range(01,12).gen();
        int year = nat.range(20,26).gen();
        String D1 = Integer.toString(date);
        String Y1 = Integer.toString(year);
        String expiryDate = (D1+"/"+Y1);
        System.out.println("Expiry Date: "+expiryDate);
        return expiryDate;
    }

    public String cvvNo() {
        CVVGenerator cvv = JRand.cvv();
        String cvvNo = cvv.gen();
        System.out.println("CVV No: "+cvvNo);
        return cvvNo;
    }

    public String CountryName() {
        CountryGenerator country = JRand.country();
        String countryName = country.gen();
        System.out.println("Country Name: "+countryName);
        return countryName;
    }

    public String cityName() {
        CityGenerator city = JRand.city();
        String cityName = city.gen();
        System.out.println("Country Name: "+cityName);
        return cityName;
    }

    public String postcode() {
        NaturalGenerator nat = JRand.natural();
        int Pincode = nat.range(300000,399999).gen();
        String pin = Integer.toString(Pincode);
        System.out.println("Pincode: "+pin);
        return pin;
    }
    public String street() {
        StreetGenerator street = new StreetGenerator();
        String Street = street.us().gen();
        System.out.println("Street: "+Street);
        return Street;
    }




}

