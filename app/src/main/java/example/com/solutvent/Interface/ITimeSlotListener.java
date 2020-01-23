package example.com.solutvent.Interface;

import java.util.List;

import example.com.solutvent.Model.TimeSlot;

public interface ITimeSlotListener {
    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();

}
