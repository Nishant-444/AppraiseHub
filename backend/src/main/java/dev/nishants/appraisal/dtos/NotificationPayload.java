package dev.nishants.appraisal.dtos;

public class NotificationPayload {
  private final String cycleName;
  private final String employeeName;
  private final String startDate;
  private final String endDate;

  public NotificationPayload(String cycleName, String employeeName,
      String startDate, String endDate) {
    this.cycleName = cycleName;
    this.employeeName = employeeName;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public String getCycleName() {
    return cycleName;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public String getStartDate() {
    return startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public static NotificationPayload forCycle(String cycleName, String startDate, String endDate) {
    return new NotificationPayload(cycleName, null, startDate, endDate);
  }

  public static NotificationPayload forCycleName(String cycleName) {
    return new NotificationPayload(cycleName, null, null, null);
  }

  public static NotificationPayload forEmployeeCycle(String employeeName, String cycleName) {
    return new NotificationPayload(cycleName, employeeName, null, null);
  }
}
