# Appointment System 2.1 Specification

## 1. Multi-Provider Healthcare Scheduling Architecture

Healthogram 2.1 unifies booking and encounter scheduling across the four clinical provider account types:
- **Doctor**: Independent clinics, specialist private practices, telemedicine.
- **Clinic**: Departmental booking, outpatient multi-doctor scheduling.
- **Hospital**: Inpatient admissions, multi-specialty consultations, emergency appointments.
- **Laboratory**: Diagnostic phlebotomy, imaging slots, and specimen collection.

---

## 2. Complete State Machine

The appointment lifecycle supports 10 deterministic states:

```
                  ┌───────────────┐
                  │   REQUESTED   │
                  └───────┬───────┘
                          │ (Provider review)
                          ▼
                  ┌───────────────┐
                  │    PENDING    │
                  └───────┬───────┘
                          │ (Slot confirmed & payment authorized)
                          ▼
                  ┌───────────────┐
       ┌─────────►│   CONFIRMED   │◄─────────┐
       │          └───────┬───────┘          │
       │ (New slot)       │                  │ (Reschedule)
┌──────┴────────┐         │          ┌───────┴───────┐
│  RESCHEDULED  │◄────────┴─────────►│  RESCHEDULED  │
└───────────────┘                    └───────────────┘
       │                                     │
       ├──────────────────┬──────────────────┤
       ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│   COMPLETED   │  │   CANCELLED   │  │    NO_SHOW    │
└───────────────┘  └───────────────┘  └───────────────┘
                          ▲
                          │ (Provider rejects initial request)
                   ┌──────┴───────┐
                   │   REJECTED   │
                   └──────────────┘
                          ▲
                          │ (Booking expires unconfirmed)
                   ┌──────┴───────┐
                   │   EXPIRED    │
                   └──────────────┘
```

---

## 3. Privacy-Preserving Push Notifications

### Strict Invariant:
**Push notifications for appointments must NEVER disclose patient medical conditions, diagnoses, or sensitive clinic department names.**

- **Violative Example**: *"Reminder: Your appointment for Chemotherapy at Oncology Dept is tomorrow."*
- **Compliant Example**: *"Your appointment with Dr. Khalfan is confirmed for Tomorrow at 10:00 AM at Consultation Room 3."*
