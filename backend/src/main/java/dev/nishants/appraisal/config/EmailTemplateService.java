package dev.nishants.appraisal.config;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateService {
  private static final String COMPANY_NAME = "AppraiseHub";

  // Appraisal cycle started
  public String cycleStarted(String employeeName, String cycleName,
      String startDate, String endDate) {
    return base(
        "Your appraisal cycle has started",
        employeeName,
        "Action required: Submit self-assessment",
        "Your performance appraisal for <strong>" + cycleName + "</strong> has been initiated.",
        dateRangeLine(startDate, endDate),
        "Please log in and complete your self-assessment before the deadline.");
  }

  // Self-assessment submitted (to manager)
  public String selfAssessmentSubmitted(String managerName, String employeeName,
      String cycleName) {
    return base(
        "Self-assessment submitted — review required",
        managerName,
        "Action required: Submit manager review",
        "<strong>" + employeeName + "</strong> has submitted their self-assessment for <strong>"
            + cycleName + "</strong>.",
        "Please review their submission and provide your rating and comments.",
        "Your timely review helps keep the appraisal process on track.");
  }

  // Manager review done (to employee)
  public String managerReviewDone(String employeeName, String cycleName) {
    return base(
        "Your appraisal has been reviewed",
        employeeName,
        "",
        "Your manager has completed their review of your appraisal for <strong>"
            + cycleName + "</strong>.",
        "The appraisal has been forwarded to HR for final approval.",
        "You will receive another notification once HR has approved the results.");
  }

  // Appraisal approved (to employee)
  public String appraisalApproved(String employeeName, String cycleName) {
    return base(
        "Your appraisal result is ready",
        employeeName,
        "Action required: View and acknowledge result",
        "Your appraisal for <strong>" + cycleName + "</strong> has been approved by HR.",
        "Please log in to view your final rating and review.",
        "Once you have reviewed the results, please acknowledge receipt.");
  }

  // Appraisal acknowledged (confirmation to employee)
  public String appraisalAcknowledged(String employeeName, String cycleName) {
    return base(
        "Appraisal acknowledged — cycle complete",
        employeeName,
        "",
        "You have successfully acknowledged your appraisal for <strong>"
            + cycleName + "</strong>.",
        "Thank you for completing the appraisal process.",
        "Your appraisal record has been saved.");
  }

  // Base HTML template (minimal)
  private String base(String heading, String recipientName, String cta,
      String... lines) {
    StringBuilder body = new StringBuilder();
    body.append("<p style=\"margin:0 0 12px;font-size:14px;\">Hi <strong>")
        .append(recipientName)
        .append("</strong>,</p>");

    for (String line : lines) {
      if (line != null && !line.isBlank()) {
        body.append("<p style=\"margin:12px 0;font-size:14px;line-height:1.6;\">")
            .append(line)
            .append("</p>");
      }
    }

    if (cta != null && !cta.isBlank()) {
      body.append("<p style=\"margin:16px 0;font-size:14px;font-weight:600;\">")
          .append(cta)
          .append("</p>");
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:#ffffff;font-family:Arial,Helvetica,sans-serif;color:#111111;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="padding:24px 16px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">
                <tr>
                  <td style="padding:4px 0 16px;">
                    <p style="margin:0;color:#6b7280;font-size:12px;">%s</p>
                    <h1 style="margin:6px 0 0;font-size:18px;font-weight:600;">%s</h1>
                  </td>
                </tr>
                <tr>
                  <td style="padding:0;">%s</td>
                </tr>
                <tr>
                  <td style="padding:18px 0 0;">
                    <p style="margin:0;color:#6b7280;font-size:12px;">
                      This is an automated message from <strong>%s</strong>. Please do not reply to this email.
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """
        .formatted(COMPANY_NAME, heading, body, COMPANY_NAME);
  }

  private String dateRangeLine(String startDate, String endDate) {
    if (startDate == null || startDate.isBlank() || endDate == null
        || endDate.isBlank()) {
      return "";
    }
    return "The cycle runs from <strong>" + startDate + "</strong> to <strong>"
        + endDate + "</strong>.";
  }
}
