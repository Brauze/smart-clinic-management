### user_stories.md
```markdown
# User Stories for Smart Clinic Management System

## Admin User Stories

1. **As an Admin**, I want to add new doctors to the system so that patients can book appointments with them.
   - Acceptance Criteria: Admin can enter doctor details (name, email, specialty) and save to database

2. **As an Admin**, I want to view a list of all doctors so that I can manage the clinic staff.
   - Acceptance Criteria: Display paginated list with search and filter by specialty

3. **As an Admin**, I want to delete doctors from the system so that I can remove inactive staff.
   - Acceptance Criteria: Soft delete with confirmation dialog, cascade handling for appointments

4. **As an Admin**, I want to monitor system activity so that I can track usage and performance.
   - Acceptance Criteria: View logs, user activities, and appointment statistics

5. **As an Admin**, I want to manage patient records so that I can maintain accurate clinic data.
   - Acceptance Criteria: CRUD operations on patient data with validation

## Patient User Stories

1. **As a Patient**, I want to register an account so that I can access the clinic services.
   - Acceptance Criteria: Registration form with email verification

2. **As a Patient**, I want to search for doctors by specialty so that I can find the right healthcare provider.
   - Acceptance Criteria: Search bar with filters for specialty, availability, and name

3. **As a Patient**, I want to book appointments with doctors so that I can receive medical care.
   - Acceptance Criteria: Calendar view showing available slots, booking confirmation

4. **As a Patient**, I want to view my appointment history so that I can track my medical visits.
   - Acceptance Criteria: List of past and upcoming appointments with details

5. **As a Patient**, I want to view my prescriptions so that I can follow my treatment plan.
   - Acceptance Criteria: List of prescriptions with medication details and instructions

6. **As a Patient**, I want to cancel appointments so that I can reschedule when needed.
   - Acceptance Criteria: Cancel button with confirmation, automatic slot release

## Doctor User Stories

1. **As a Doctor**, I want to login to my dashboard so that I can manage my practice.
   - Acceptance Criteria: Secure login with JWT token authentication

2. **As a Doctor**, I want to set my available times so that patients can book appointments.
   - Acceptance Criteria: Calendar interface to set weekly availability patterns

3. **As a Doctor**, I want to view my appointments so that I can prepare for patient visits.
   - Acceptance Criteria: Daily/weekly/monthly view with patient details

4. **As a Doctor**, I want to create prescriptions for patients so that I can provide treatment.
   - Acceptance Criteria: Prescription form with medication database, dosage calculator

5. **As a Doctor**, I want to view patient medical history so that I can make informed decisions.
   - Acceptance Criteria: Access to past appointments, prescriptions, and notes

6. **As a Doctor**, I want to update appointment status so that I can track completed visits.
   - Acceptance Criteria: Status update buttons (completed, cancelled, no-show)