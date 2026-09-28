### 3.2.12. AI & Supporting Services

#### 3.2.12.1. Chat with AI

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-012 - Chat with AI | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Gemini AI Service |
| Trigger | User navigates to the AI Assistant section and types a question or request in the chat interface. | | |
| Description | Enables the User to interact with the Gemini AI Service through a conversational chat interface for food safety guidance, menu planning assistance, nutritional queries, and system support. | | |
| Preconditions | User is authenticated and authorized. Gemini AI Service is operational and configured (BR-157). | | |
| Postconditions | AI-generated response displayed in chat. Full conversation exchange archived for compliance audits (BR-157). | | |
| Normal Flow | 1. User navigates to Chat with AI section. 2. System displays chat interface with previous conversation history. 3. User types a question or request and submits. 4. System sends query to Gemini AI Service with relevant context (user role, active policies, nutritional data per BR-156). 5. Gemini AI returns response. System displays it in chat and archives the complete exchange (BR-157). | | |
| Alternative Flows | AF1 - AI Service Unavailable: Gemini AI Service unreachable. System displays service unavailability message and advises user to retry later. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-143 Chat with AI Use Cases Specification

#### 3.2.12.2. Manage External Integrations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-040 - Manage External Integrations | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to manage external integrations. | | |
| Description | Enables the System Admin to manage details, modify parameters, or update configuration fields of External Integrations. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The manage external integrations transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. System Admin navigates to the 'External Integrations' panel.  2. System displays a list of connected third-party services: GPS Service, Gmail SMTP, Firebase Cloud Messaging, and Gemini AI.  3. System Admin selects a service and clicks 'Edit Configuration'.  4. System Admin updates the API endpoint, OAuth2 client credentials, or API secret key.  5. System Admin clicks 'Test Connection'.  6. System performs test handshake using OAuth2 or the new key (BR-163) and returns 'Success'.  7. System Admin clicks 'Save'.  8. System encrypts credentials, saves settings (MSG-043), logs action to the immutable audit trail (BR-162), and displays success message. | | |
| Alternative Flows | AF1 - Connection Test Failed: At step 6, connection test fails. System displays error message detailing failure (e.g. Invalid API key) and prevents saving. | | |
| Exceptions | EX1 - Missing Mandatory Fields: System Admin attempts to save with empty key or endpoint. System displays validation error (MSG-004) and blocks action. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-144 Manage External Integrations Use Cases Specification

#### 3.2.12.3. Monitor System Activities

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-057 - Monitor System Activities | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to monitor system activities. | | |
| Description | Enables the System Admin to safely query, view, or export the details of System Activities records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The monitor system activities data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the System Activities section.  2. System queries server performance APIs and retrieves active statistics (CPU load, memory load, disk IO, active user sessions, database query pool load).  3. System displays charts and indicators showing current metrics.  4. System compiles and displays weekly active user metrics and system health summary reports (BR-165). | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Dashboard Load Failure: Server monitoring API is unreachable. System displays error message indicating service monitoring agent is offline. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-179 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-145 Monitor System Activities Use Cases Specification

#### 3.2.12.4. Receive Notification

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-058 - Receive Notification | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Firebase Cloud Message |
| Trigger | System detects a triggering event (e.g., menu published, incident reported, allergy alert) and initiates push notification delivery via Firebase Cloud Messaging. | | |
| Description | Enables the User to receive real-time push notifications for relevant system events including menu updates, food safety alerts, allergy warnings, and incident reports. | | |
| Preconditions | User is authenticated and has push notifications enabled. Firebase Cloud Messaging is operational. A triggering event has occurred in the system. | | |
| Postconditions | Notification delivered to user's device. Delivery status and timestamp logged. Retry initiated if delivery fails. | | |
| Normal Flow | 1. System detects a triggering event (menu published, incident reported, etc.). 2. System checks target user's notification preferences and quiet hours (default: 10PM-6AM per BR-158). 3. System constructs notification payload and dispatches via Firebase Cloud Messaging. 4. Firebase Cloud Messaging delivers push notification to user's registered device. 5. System logs delivery status, timestamp, and event type for audit purposes. | | |
| Alternative Flows | AF1 - Delivery Failure: FCM delivery fails. System automatically retries (MSG-068). After exhausting retries, system logs the failure and attempts email fallback delivery (BR-159). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and push notifications enabled on their device. | | |

Table 3-146 Receive Notification Use Cases Specification

#### 3.2.12.5. Scan Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-079 - Scan Face Data | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Primary actor requests to scan face data. | | |
| Description | Enables the Service Caterers to successfully perform the Scan Face Data operations in the system. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The scan face data transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Service Caterers staff accesses the face scanning module.  2. System activates the camera interface and prompts the user (e.g. delivery driver) to look at the camera.  3. System captures video frames and detects face presence.  4. System extracts facial landmarks and generates the biometric face template.  5. System compares the template with the registered profile for validation (BR-050).  6. System displays 'Verification Successful' and logs the match with timestamp. | | |
| Alternative Flows | AF1 - Register Driver Biometrics: At step 1, supplier driver is onboarding. Service Caterers captures face scan, associates biometric template with driver profile, and saves (MSG-043) for future delivery validation. | | |
| Exceptions | EX1 - Face Recognition Failed: Biometric identity could not be verified due to lighting or mismatch. System displays error MSG-014 (Face recognition failed) and prompts to retry or enter passcode verification. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-147 Scan Face Data Use Cases Specification

#### 3.2.12.6. Turn off/Turn on Notification

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-088 - Turn off/Turn on Notification | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Firebase Cloud Message |
| Trigger | User navigates to Notification Settings and adjusts notification preferences. | | |
| Description | Enables the User to enable or disable specific push notification categories and configure quiet hours to control the timing of system notifications. | | |
| Preconditions | User is authenticated and has an active session. | | |
| Postconditions | Notification preferences saved. All subsequent notifications respect the updated configuration including quiet hours (BR-158). | | |
| Normal Flow | 1. User navigates to Settings and selects 'Notification Preferences'. 2. System displays current notification configuration: enabled categories and quiet hours (default: 10:00 PM - 6:00 AM per BR-158). 3. User toggles notification categories on/off or adjusts quiet hours settings. 4. System saves the updated configuration to the user profile. 5. System confirms the update. All future notifications will respect the new configuration. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-148 Turn off/Turn on Notification Use Cases Specification

#### 3.2.12.7. View Audit Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-106 - View Audit Logs | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view audit logs. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Audit Logs records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view audit logs data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Audit Logs panel.  2. System queries the immutable audit log server (BR-162) and retrieves recent logs sorted by timestamp.  3. System displays log entries showing: Timestamp, User ID, IP Address, Action Performed, Resource Accessed, and Status.  4. System Admin applies filters: Date range, User ID, Action category, or IP address.  5. System retrieves filtered records and displays them in a read-only list. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-175 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-149 View Audit Logs Use Cases Specification

#### 3.2.12.8. View Error Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-111 - View Error Logs | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view error logs. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Error Logs records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view error logs data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Error Logs section.  2. System retrieves error logs from the logging database sorted by severity and timestamp.  3. System displays a list showing: Timestamp, Error Message, Severity, Module/Class, and Path.  4. System Admin selects a specific error.  5. System displays the detailed stack trace, environment variables, request metadata (headers, query parameters), and affected user session info (BR-164). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-176 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-150 View Error Logs Use Cases Specification

#### 3.2.12.9. View History Chat

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-119 - View History Chat | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view history chat. | | |
| Description | Enables the User to safely query, view, or export the details of History Chat records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view history chat data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the 'AI Chat History' section.  2. System queries the archived AI interaction log (BR-157) filtering by the user's ID.  3. System displays a list of past chat sessions sorted by date.  4. User selects a chat session.  5. System retrieves and renders the complete dialogue history of questions and AI responses. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-151 View History Chat Use Cases Specification

#### 3.2.12.10. View Notification History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-136 - View Notification History | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view notification history. | | |
| Description | Enables the User to safely query, view, or export the details of Notification History records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view notification history data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the Notification History section.  2. System queries the notification database and retrieves received notifications for the user's ID sorted by dispatch timestamp.  3. System displays the list showing: Title, Body, Date, Type (Alert/Notification), and Read/Unread Status.  4. User can search by keyword or filter by Read/Unread status. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-152 View Notification History Use Cases Specification

#### 3.2.12.11. View System Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-149 - View System Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view system status. | | |
| Description | Enables the System Admin to safely query, view, or export the details of System Status records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view system status data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. Actor: System Admin navigates to access the System Status panel. 2. System: Authenticate access and immediately initiate online health checks for core services. 3. System: Returns results and displays them on the connection health dashboard. 4. System: Load and display the configuration and operating status of the weekly backup scheduler (BR-160). 5. Actor: Observe and monitor the parameters on the dashboard. | | |
| Alternative Flows | 4a. Export System Status Records:   1. Actor: In the System Status console, the System Admin clicks the "Export" function. 2. System: Summarizes the current status parameters (including status, latencies, and backup scheduler status). 3. System: Exports the data to a file (e.g.,.CSV or.PDF) and initiates the download process to the System Admin's device. 4. The event flow returns to step 5 of the Normal Flow. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-177 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-153 View System Status Use Cases Specification

#### 3.2.12.12. View Usage Statistics

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-150 - View Usage Statistics | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view usage statistics. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Usage Statistics records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view usage statistics data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Usage Statistics panel.  2. System queries system log aggregation database.  3. System displays dashboards showing active daily/weekly users, top use cases called, storage capacity utilization, and API request count logs (BR-165).  4. System Admin filters metrics by date range or specific user role categories.  5. System refreshes the graphs and tables. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-178 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-154 View Usage Statistics Use Cases Specification

#### 3.2.12.13. Configure Security Policies

*(Bổ sung 2026-07-01 — UC mới, phát hiện qua audit A8 của `00_TODO_GiaDinh_Master.md`: màn hình `Web/SystemAdmin/15_SecurityPolicies.md` mô tả cấu hình BR-002/BR-004/BR-006 nhưng không có UC nguồn nào trong Report 3/traceability_matrix. BR-002/004/006 trước đây chỉ được nêu là hằng số nghiệp vụ, chưa có UC cho phép System Admin xem/sửa. UC dưới đây được đánh số UC-184, tiếp theo UC-183 là mã UC lớn nhất đã cấp — xem traceability_matrix.md §1.13.)*

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-184 - Configure Security Policies | | |
| Created By | Claude (audit A8, xác nhận bởi team) | Date Created | 01/07/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin requests to view or update platform-wide security policy parameters. | | |
| Description | Enables the System Admin to view and adjust security-related configuration parameters governing account lockout, session timeout, and password strength requirements across the platform. | | |
| Preconditions | The System Admin is authenticated and authorized to access System Configuration. | | |
| Postconditions | Security policy parameters are updated, saved, and recorded in the audit trail. New values apply platform-wide on the next relevant security check (login attempt, session check, or password change). | | |
| Normal Flow | 1. System Admin navigates to the Security Policies panel (System Configuration group). 2. System displays current values: maximum failed login attempts before lockout (BR-002), lockout duration (BR-002), session timeout duration (BR-004), and password strength rules (BR-006). 3. System Admin edits one or more parameters. 4. System validates the new values are within allowed ranges. 5. System saves the updated parameters, records the change in the audit trail (BR-170), and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If a new value is outside the allowed range, system displays a validation error and does not save. | | |
| Priority | Low | | |
| Frequency of Use | Rare | | |
| Business Rules | BR-002, BR-004, BR-006, BR-170 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. Values shown are the platform's current hard-coded defaults (5 lần sai/15 phút lockout, 30 phút session timeout) until this UC is implemented; team cần xác nhận phạm vi cho phép chỉnh sửa (toàn bộ hay giới hạn khoảng cho phép) trước khi code hoá. | | |

Table 3-155 Configure Security Policies Use Cases Specification

