package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.Contact;
import org.springframework.stereotype.Component;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class BookingArrangementMapper {


    public BookingArrangement map(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);
        BookingArrangement entity = new BookingArrangement();
        input.apply(FIELD_BOOKING_CONTACT, this::mapContact, entity::setBookingContact);

        input.apply(FIELD_BOOK_WHEN, entity::setBookWhen);
        input.apply(FIELD_BUY_WHEN, entity::setBuyWhen);
        input.apply(FIELD_BOOKING_ACCESS, entity::setBookingAccess);
        input.apply(FIELD_BOOKING_METHODS, entity::setBookingMethods);
        input.apply(FIELD_BOOKING_NOTE, entity::setBookingNote);
        input.apply(FIELD_LATEST_BOOKING_TIME, entity::setLatestBookingTime);
        input.apply(FIELD_MINIMUM_BOOKING_PERIOD, entity::setMinimumBookingPeriod);
        return entity;
    }

    private Contact mapContact(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);
        Contact entity = new Contact();

        input.apply(FIELD_CONTACT_PERSON, entity::setContactPerson);
        input.apply(FIELD_PHONE, entity::setPhone);
        input.apply(FIELD_URL, entity::setUrl);
        input.apply(FIELD_EMAIL, entity::setEmail);
        input.apply(FIELD_FURTHER_DETAILS, entity::setFurtherDetails);

        return entity;
    }
}
