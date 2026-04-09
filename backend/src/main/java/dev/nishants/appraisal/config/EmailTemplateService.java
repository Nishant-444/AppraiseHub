package dev.nishants.appraisal.config;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateService {
  private static final String PRIMARY_COLOR = "#7c3aed";
  private static final String COMPANY_NAME = "Appraisaly";

  // ── Appraisal cycle started ───────────────────────────────────
  public String cycleStarted(String employeeName, String cycleName,
      String startDate, String endDate) {
    return base(
        "Your appraisal cycle has started",
        employeeName,
        "Your performance appraisal for <strong>" + cycleName + "</strong> has been initiated.",
        "The cycle runs from <strong>" + startDate + "</strong> to <strong>" + endDate + "</strong>.",
        "Please log in and complete your self-assessment before the deadline.",
        "Action required: Submit self-assessment");
  }

  // ── Self-assessment submitted (to manager) ────────────────────
  public String selfAssessmentSubmitted(String managerName, String employeeName,
      String cycleName) {
    return base(
        "Self-assessment submitted — review required",
        managerName,
        "<strong>" + employeeName + "</strong> has submitted their self-assessment for <strong>"
            + cycleName + "</strong>.",
        "Please review their submission and provide your rating and comments.",
        "Your timely review helps keep the appraisal process on track.",
        "Action required: Submit manager review");
  }

  // ── Manager review done (to employee) ────────────────────────
  public String managerReviewDone(String employeeName, String cycleName) {
    return base(
        "Your appraisal has been reviewed",
        employeeName,
        "Your manager has completed their review of your appraisal for <strong>"
            + cycleName + "</strong>.",
        "The appraisal has been forwarded to HR for final approval.",
        "You will receive another notification once HR has approved the results.",
        "No action required yet");
  }

  // ── Appraisal approved (to employee) ─────────────────────────
  public String appraisalApproved(String employeeName, String cycleName) {
    return base(
        "Your appraisal result is ready",
        employeeName,
        "Your appraisal for <strong>" + cycleName + "</strong> has been approved by HR.",
        "Please log in to view your final rating and review.",
        "Once you have reviewed the results, please acknowledge receipt.",
        "Action required: View and acknowledge result");
  }

  // ── Appraisal acknowledged (confirmation to employee) ────────
  public String appraisalAcknowledged(String employeeName, String cycleName) {
    return base(
        "Appraisal acknowledged — cycle complete",
        employeeName,
        "You have successfully acknowledged your appraisal for <strong>"
            + cycleName + "</strong>.",
        "Thank you for completing the appraisal process.",
        "Your appraisal record has been saved.",
        "No further action required");
  }

  // ── Base HTML template ────────────────────────────────────────
  private String base(String heading, String recipientName,
      String line1, String line2, String line3, String cta) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 16px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                <!-- Header -->
                <tr>
                  <td style="background:%s;border-radius:12px 12px 0 0;padding:28px 32px;">
                    <p style="margin:0;color:#ffffff;font-size:13px;opacity:0.85;">%s</p>
                    <h1 style="margin:8px 0 0;color:#ffffff;font-size:22px;font-weight:600;line-height:1.3;">%s</h1>
                  </td>
                </tr>

                <!-- Body -->
                <tr>
                  <td style="background:#ffffff;padding:32px;">
                    <p style="margin:0 0 8px;color:#374151;font-size:15px;">Hi <strong>%s</strong>,</p>
                    <p style="margin:16px 0;color:#374151;font-size:14px;line-height:1.7;">%s</p>
                    <p style="margin:16px 0;color:#374151;font-size:14px;line-height:1.7;">%s</p>
                    <p style="margin:16px 0;color:#374151;font-size:14px;line-height:1.7;">%s</p>

                    <!-- CTA box -->
                    <div style="margin:24px 0;background:#f3f0ff;border-left:4px solid %s;border-radius:0 8px 8px 0;padding:14px 18px;">
                      <p style="margin:0;color:#5b21b6;font-size:13px;font-weight:600;">%s</p>
                    </div>
                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="background:#f9fafb;border-radius:0 0 12px 12px;padding:20px 32px;border-top:1px solid #e5e7eb;">
                    <p style="margin:0;color:#9ca3af;font-size:12px;">
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
        .formatted(
            PRIMARY_COLOR, COMPANY_NAME, heading,
            recipientName,
            line1, line2, line3,
            PRIMARY_COLOR, cta,
            COMPANY_NAME);
  }
}
