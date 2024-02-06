package io.castled.android.notifications

import java.util.Date

class CastledUserAttributes {

    companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val EMAIL = "email"
        const val NAME = "name"
        const val DOB = "date_of_birth"
        const val GENDER = "gender"
        const val PHONE_NUMBER = "phone_number"
        const val CITY = "city"
        const val COUNTRY = "country"
    }

    private val attributes = mutableMapOf<String, Any?>()

    fun setName(name: String?) {
        attributes[NAME] = name
    }

    fun setFirstName(firstName: String?) {
        attributes[FIRST_NAME] = firstName
    }

    fun setLastName(lastName: String?) {
        attributes[LAST_NAME] = lastName
    }

    fun setEmail(email: String?) {
        attributes[EMAIL] = email
    }

    fun setDOB(dob: Date?) {
        attributes[DOB] = dob
    }

    fun setGender(gender: String?) {
        attributes[GENDER] = gender
    }

    fun setPhone(phone: String?) {
        attributes[PHONE_NUMBER] = phone
    }

    fun setCity(city: String?) {
        attributes[CITY] = city
    }

    fun setCountry(country: String?) {
        attributes[COUNTRY] = country
    }

    fun setCustomAttribute(key: String, value: Any?) {
        attributes[key] = value
    }

    fun getAttributes() = attributes

    fun setAttributes(attrs: Map<String, Any?>) = attributes.putAll(attrs)

}