package com.amazon.ata.metrics.classroom.activity;

import com.amazon.ata.metrics.classroom.dao.ReservationDao;
import com.amazon.ata.metrics.classroom.dao.models.Reservation;
import com.amazon.ata.metrics.classroom.metrics.MetricsConstants;
import com.amazon.ata.metrics.classroom.metrics.MetricsPublisher;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import javax.inject.Inject;

/**
 * Handles requests to book a reservation.
 */
public class BookReservationActivity {

    private ReservationDao reservationDao;
    private MetricsPublisher metricsPublisher;

    /**
     * Constructs a BookReservationActivity
     * @param reservationDao Dao used to create reservations.
     */
    @Inject
    public BookReservationActivity(ReservationDao reservationDao, MetricsPublisher metricsPublisher) {
        this.reservationDao = reservationDao;
        this.metricsPublisher = metricsPublisher;
    }

    /**
     * Creates a reservation with the provided details and updates the BookedReservationCount and
     * the ReservationRevenue metrics.
     * @param reservation Reservation to create.
     * @return
     */
    public Reservation handleRequest(Reservation reservation) {
        Reservation response = reservationDao.bookReservation(reservation);
        // create a new reservation

        metricsPublisher.addMetric(MetricsConstants.BOOKED_RESERVATION_COUNT, 1,
                StandardUnit.Count);
        // update the BookedReservationMetric count
        // class-of-enum.enum-name for MetricsConstants.BOOKED_RESERVATION_COUNT

        metricsPublisher.addMetric(MetricsConstants.RESERVATION_REVENUE,
                response.getTotalCost().doubleValue(), StandardUnit.None);
        // update the ReservationRevenue metric with the total cost of the reservation
        // it's stored in response upon return from the ReservationDao
        // convert the BigDecimal (response.getTotalCost()) to a double
        // the totalCost in the reservation is negative if we lost revenue ... that means to us that
        //  we don't have to change it ... we just have to store it ... if they didn't make it
        //  negative, we'd have to make it negative using a (-) at the front of the line <3

        return response;
    }

}
