package com.amazon.ata.metrics.classroom.activity;

import com.amazon.ata.metrics.classroom.dao.ReservationDao;
import com.amazon.ata.metrics.classroom.dao.models.UpdatedReservation;
import com.amazon.ata.metrics.classroom.metrics.MetricsConstants;
import com.amazon.ata.metrics.classroom.metrics.MetricsPublisher;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.time.ZonedDateTime;
import javax.inject.Inject;

/**
 * Handles requests to modify a reservation
 */
public class ModifyReservationActivity {

    private ReservationDao reservationDao;
    private MetricsPublisher metricsPublisher;

    /**
     * Construct ModifyReservationActivity.
     * @param reservationDao Dao used for modify reservations.
     */
    @Inject
    public ModifyReservationActivity(ReservationDao reservationDao, MetricsPublisher metricsPublisher) {
        this.reservationDao = reservationDao;
        this.metricsPublisher = metricsPublisher;
    }

    /**
     * Modifies the given reservation and updates the ModifiedReservationCount and the
     * ReservationRevenue metrics.
     * @param reservationId id to modify reservations for
     * @param checkInDate modified check in date
     * @param numberOfNights modified number of nights
     * @return UpdatedReservation that includes the old reservation and the updated reservation details.
     */
    public UpdatedReservation handleRequest(final String reservationId, final ZonedDateTime checkInDate,
                                            final Integer numberOfNights) {
        UpdatedReservation updatedReservation = reservationDao.modifyReservation(reservationId,
                checkInDate, numberOfNights);
        // modify the reservation

        metricsPublisher.addMetric(MetricsConstants.MODIFIED_RESERVATION_COUNT, 1,
                StandardUnit.Count);
        // updates the ModifiedReservationCount metric

        double revenueDifference = updatedReservation.getModifiedReservation().getTotalCost()
                .subtract(updatedReservation.getOriginalReservation().getTotalCost()).doubleValue();

        metricsPublisher.addMetric(MetricsConstants.RESERVATION_REVENUE, revenueDifference,
                StandardUnit.None);
        // update the ReservationRevenue metric with the total cost of the reservation
        // the UpdatedReservation is stored in response upon return from the ReservationDao and
        //  contains the original reservation and the modified reservation
        // if we subtract the totalCost from the original reservation from the modified reservation
        //  we will have the difference in revenue which is what they want us to log <3

        return updatedReservation;
    }

}
