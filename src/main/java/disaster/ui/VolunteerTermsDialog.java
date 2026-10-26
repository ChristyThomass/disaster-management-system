package disaster.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog presenting the Volunteer Agreement and Terms of Service
 * with liability waiver and operational protocols before registration.
 */
public class VolunteerTermsDialog extends JDialog {

    private final Runnable onAccept;
    private JCheckBox agreeCheckbox;
    private JButton agreeButton;

    public VolunteerTermsDialog(Window parent, Runnable onAccept) {
        super(parent, "Volunteer Agreement & Terms of Service - SDMA", ModalityType.APPLICATION_MODAL);
        this.onAccept = onAccept;
        setSize(760, 560);
        setMinimumSize(new Dimension(600, 440));
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Banner
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        badge.setBackground(UITheme.HERO_BADGE_BG);
        badge.setBorder(new LineBorder(new Color(UITheme.BRAND_CRIMSON.getRed(), UITheme.BRAND_CRIMSON.getGreen(), UITheme.BRAND_CRIMSON.getBlue(), 120), 1, true));
        JLabel badgeLbl = new JLabel("SDMA STATE DISASTER RESPONSE CORPS");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLbl.setForeground(UITheme.BRAND_CRIMSON);
        badge.add(badgeLbl);
        badgeRow.add(badge);
        header.add(badgeRow);

        header.add(Box.createVerticalStrut(8));

        JLabel titleLbl = new JLabel("Volunteer Agreement and Terms of Service");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(UITheme.TEXT);
        header.add(titleLbl);

        JLabel subLbl = new JLabel("Review operational responsibilities, safety guidelines, and comprehensive liability waiver.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(UITheme.SECONDARY_TEXT);
        header.add(subLbl);

        root.add(header, BorderLayout.NORTH);

        // 2. Terms Text in Scroll Area
        JTextArea termsText = new JTextArea();
        termsText.setEditable(false);
        termsText.setLineWrap(true);
        termsText.setWrapStyleWord(true);
        termsText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        termsText.setForeground(new Color(33, 37, 41));
        termsText.setBackground(Color.WHITE);
        termsText.setMargin(new Insets(16, 18, 16, 18));
        termsText.setText(getTermsAndConditionsContent());
        termsText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(termsText);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Consent & Action Bar
        JPanel bottomCard = new JPanel();
        bottomCard.setLayout(new BoxLayout(bottomCard, BoxLayout.Y_AXIS));
        bottomCard.setBackground(Color.WHITE);
        bottomCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        agreeCheckbox = new JCheckBox("I have read, understood, and agree to the Volunteer Terms of Service & Comprehensive Liability Waiver.");
        agreeCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 13));
        agreeCheckbox.setForeground(UITheme.TEXT);
        agreeCheckbox.setOpaque(false);
        agreeCheckbox.setFocusPainted(false);
        bottomCard.add(agreeCheckbox);

        bottomCard.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);

        JButton cancelBtn = BaseFrame.createOutlinedButtonStatic("Decline / Cancel", UITheme.BORDER_DARK, UITheme.TEXT);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setPreferredSize(new Dimension(150, 42));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(cancelBtn);

        agreeButton = BaseFrame.createSolidButton("I Agree & Proceed to Registration ->", UITheme.BRAND_CRIMSON, Color.WHITE);
        agreeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        agreeButton.setPreferredSize(new Dimension(300, 42));
        agreeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        agreeButton.addActionListener(e -> {
            if (!agreeCheckbox.isSelected()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please check the 'I Agree' box to confirm your electronic agreement\nand acknowledgement before proceeding to volunteer registration.",
                        "Agreement Required",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            dispose();
            if (onAccept != null) {
                onAccept.run();
            }
        });
        btnRow.add(agreeButton);

        bottomCard.add(btnRow);
        root.add(bottomCard, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private String getTermsAndConditionsContent() {
        return "Volunteer Agreement and Terms of Service\n\n"
                + "Welcome to the SDMA (State Disaster Management Authority) Disaster Management System. By registering as a volunteer, accessing the platform, or participating in any deployment operations, you agree to be legally bound by these Terms and Conditions. Please read this document thoroughly, as it affects your legal rights, outlines your operational responsibilities, and includes a comprehensive waiver of liability.\n\n"
                + "1. Nature of Volunteer Relationship\n"
                + "1.1 Voluntary Service: You acknowledge that your participation is entirely voluntary and undertaken of your own free will. You are acting as an unpaid volunteer, not as an employee, contractor, consultant, or agent of SDMA or its partners.\n"
                + "1.2 No Compensation or Benefits: You understand that you will not receive any financial compensation, stipends, health insurance, workers' compensation benefits, or unemployment benefits for your time, services, or any expenses incurred during your volunteer duties.\n"
                + "1.3 At-Will Participation: Your volunteer status is \"at-will,\" meaning either you or the organization may terminate the volunteer relationship at any time, for any reason, with or without notice.\n\n"
                + "2. Eligibility and Registration Requirements\n"
                + "2.1 Accuracy of Information: You certify that all information provided during your account creation (including your legal name, contact details, medical conditions, and emergency contacts) is true, accurate, and current. You agree to update your profile immediately if this information changes.\n"
                + "2.2 Age Requirement: You must be at least 18 years of age to register as a frontline deployment volunteer. (If applicable: Volunteers between 16 and 17 may perform remote, non-hazardous tasks with explicit parental consent).\n"
                + "2.3 Background Checks: You consent to SDMA conducting background checks, identity verification, or credential validation prior to granting you full access to the system.\n\n"
                + "3. Assumption of Risk and Comprehensive Liability Waiver\n"
                + "3.1 Acknowledgment of Inherent Risks: You fully understand that disaster relief operations involve severe, unpredictable, and inherent risks. These risks include, but are not limited to: structural collapse, hazardous materials, severe weather, flooding, fire, lack of access to medical care, psychological trauma, physical injury, illness, and death.\n"
                + "3.2 Fitness to Serve: You certify that you are physically and psychologically fit to participate in disaster response. You assume full responsibility for managing your own health, medications, and physical limitations.\n"
                + "3.3 Complete Release of Liability: You hereby fully and forever release, discharge, indemnify, and hold harmless SDMA, its directors, officers, software developers, affiliated agencies, and other volunteers from any and all liability, claims, demands, or causes of action arising out of any damage, loss, injury, or death that occurs during your volunteer service, whether caused by negligence or otherwise.\n\n"
                + "4. Operational Protocols and Chain of Command\n"
                + "4.1 Strict Adherence to Orders: During any disaster response, you agree to strictly follow the chain of command. You will obey all instructions given by authorized Incident Commanders, system administrators, and emergency personnel.\n"
                + "4.2 Prohibition of Self-Deployment: You explicitly agree never to self-deploy to a disaster zone, SOS location, or incident scene without receiving an official dispatch order through the Disaster Management System. Unauthorized deployment endangers you and emergency responders.\n"
                + "4.3 System Misuse: You will not use the system to submit false SOS requests, spam the incident reporting board, or interfere with live rescue operations.\n\n"
                + "5. Confidentiality and Data Protection\n"
                + "5.1 Protection of Sensitive Data: Through the system, you may have access to highly sensitive information, including the real-time GPS locations of victims in distress, medical needs, and Personally Identifiable Information (PII).\n"
                + "5.2 Strict Non-Disclosure: You are legally bound to keep all system data completely confidential. You will not copy, export, distribute, or discuss any victim data, SOS requests, or operational logistics with anyone outside the official chain of command.\n"
                + "5.3 Social Media Ban: You agree not to post, broadcast, or stream live operational details, victim identities, or photographs of active disaster scenes to any social media platform, press outlet, or public forum.\n\n"
                + "6. Code of Conduct and Professionalism\n"
                + "6.1 Zero Tolerance for Harassment: You will treat all survivors, fellow volunteers, and emergency workers with absolute dignity and respect. Discrimination, harassment, or abuse of any kind (based on race, gender, religion, nationality, or disability) will result in immediate termination and potential legal action.\n"
                + "6.2 Substance Policy: You are strictly prohibited from consuming, possessing, or being under the influence of alcohol, illegal drugs, or impairing medications while deployed or while operating the Disaster Management System.\n"
                + "6.3 Public Representation: While acting as a volunteer, you represent the organization. You agree not to make any public statements on behalf of the organization without prior written consent from the administration.\n\n"
                + "7. Intellectual Property and Media Release\n"
                + "7.1 Data Ownership: Any data, reports, or incident logs you input into the Disaster Management System remain the exclusive property of SDMA.\n"
                + "7.2 Photographic Release: You grant the organization an irrevocable, royalty-free license to use any photographs, video recordings, or audio recordings taken of you during your volunteer service for training, reporting, and promotional purposes.\n\n"
                + "8. Account Suspension and Termination\n"
                + "8.1 Revocation of Access: SDMA reserves the absolute right to suspend your system access, delete your account, or remove you from an active deployment if you violate any clause of this agreement, exhibit unsafe behavior, or compromise the integrity of the system.\n\n"
                + "9. Governing Law\n"
                + "This Agreement shall be governed by and construed in accordance with the laws of Kerala, India. Any disputes arising from this agreement shall be resolved in the exclusive jurisdiction of the courts located in Kerala, India.\n\n"
                + "By checking the \"I Agree\" box and completing your registration, you electronically sign this document and acknowledge that you have read, fully understand, and agree to be legally bound by all terms, conditions, and waivers contained herein.";
    }
}
