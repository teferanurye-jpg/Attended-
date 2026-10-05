package com.example.util

enum class AppLanguage {
    AMHARIC,
    ENGLISH
}

object LanguageManager {
    var currentLanguage = AppLanguage.AMHARIC

    fun isAmharic(): Boolean = currentLanguage == AppLanguage.AMHARIC
}

object AppStrings {
    // Top Bar & Branding
    val appTitle: String get() = if (LanguageManager.isAmharic()) "ኢትዮ አቴንዳንስ" else "Ethio Attendance"
    val companyName: String get() = if (LanguageManager.isAmharic()) "የኢትዮጵያ ኩባንያ" else "ETHIO COMPANY"
    val officialBadge: String get() = if (LanguageManager.isAmharic()) "ዲጂታል የሠራተኛ መታወቂያ" else "OFFICIAL DIGITAL BADGE"
    
    // Bottom Nav Tabs
    val tabPunch: String get() = if (LanguageManager.isAmharic()) "ሰዓት ምዝገባ" else "Punch"
    val tabBranches: String get() = if (LanguageManager.isAmharic()) "ቅርንጫፎች" else "Branches"
    val tabHistory: String get() = if (LanguageManager.isAmharic()) "የክትትል ታሪክ" else "Timesheet"
    val tabProfile: String get() = if (LanguageManager.isAmharic()) "መታወቂያ" else "Profile"

    // Dashboard Greetings & Time
    fun greeting(name: String): String = if (LanguageManager.isAmharic()) "ሰላም, $name" else "Selam, $name"
    val ethioLocalTimeLabel: String get() = if (LanguageManager.isAmharic()) "የኢትዮጵያ ሰዓት አቆጣጠር" else "Ethiopian Local Time"
    val shiftLabel: String get() = if (LanguageManager.isAmharic()) "መደበኛ የሥራ ሰዓት፡ ከጧቱ 2:30 - ከሰዓት 11:30" else "Standard Shift: 8:30 AM - 5:30 PM"
    
    // Status Badges
    val statusClockedIn: String get() = if (LanguageManager.isAmharic()) "በሥራ ላይ (ገባ)" else "CLOCKED IN (ACTIVE)"
    val statusOnBreak: String get() = if (LanguageManager.isAmharic()) "በእረፍት ላይ" else "ON BREAK"
    val statusClockedOut: String get() = if (LanguageManager.isAmharic()) "ተጠናቋል (ወጣ)" else "CLOCKED OUT"

    // Location Radar
    val locationTitle: String get() = if (LanguageManager.isAmharic()) "የቀጥታ የቦታ መገኛ (ጂፒኤስ)" else "Real-Time Location Tagging"
    val locationLiveSubtitle: String get() = if (LanguageManager.isAmharic()) "የሳተላይት መገኛ ነቅቷል" else "Live Satellite GPS Active"
    val locationSimSubtitle: String get() = if (LanguageManager.isAmharic()) "የሙከራ መገኛ (ዋና መ/ቤት)" else "Testing Site (HQ Demo Tag)"
    val coordinatesLabel: String get() = if (LanguageManager.isAmharic()) "የቦታ መጋጠሚያ (ኮኦርዲኔት)" else "GPS COORDINATES"
    val accuracyLabel: String get() = if (LanguageManager.isAmharic()) "ትክክለኛነት" else "ACCURACY"
    val acquiringGps: String get() = if (LanguageManager.isAmharic()) "ሳተላይት በመፈለግ ላይ..." else "Acquiring GPS fix..."
    val refreshGps: String get() = if (LanguageManager.isAmharic()) "አድስ" else "Refresh"
    val changeBranch: String get() = if (LanguageManager.isAmharic()) "ቀይር" else "Change"
    val simulateHq: String get() = if (LanguageManager.isAmharic()) "በዋናው መ/ቤት ሞክር" else "Simulate at HQ Office"

    // Geofence states
    fun insideWorkZone(distanceMeters: Int): String =
        if (LanguageManager.isAmharic()) "በሥራ ቦታ ክልል ውስጥ ($distanceMeters ሜትር ከቢሮ)" else "Within Work Zone (${distanceMeters}m from office center)"
    val insideWorkZoneGeneric: String get() = if (LanguageManager.isAmharic()) "በሥራ ቦታ ክልል ውስጥ ገብተዋል" else "Within Work Zone Geofence"
    fun outsideWorkZone(distanceStr: String): String =
        if (LanguageManager.isAmharic()) "ከሥራ ቦታ ክልል ውጪ ($distanceStr ርቀት ላይ)" else "Outside Geofence ($distanceStr away)"

    // Attendance Actions
    val clockInHeader: String get() = if (LanguageManager.isAmharic()) "የመግቢያና መውጫ ሰዓት መመዝገቢያ" else "ATTENDANCE CLOCK"
    val clockInOnSiteButton: String get() = if (LanguageManager.isAmharic()) "መግቢያ መዝግብ (በቢሮ ቅጥር ግቢ)" else "PUNCH CLOCK-IN (ON-SITE)"
    val clockInRemoteButton: String get() = if (LanguageManager.isAmharic()) "መግቢያ መዝግብ (ከቢሮ ውጪ / በመስክ)" else "PUNCH CLOCK-IN (REMOTE)"
    val clockOutButton: String get() = if (LanguageManager.isAmharic()) "መውጫ መዝግብ" else "Clock Out"
    val startBreakButton: String get() = if (LanguageManager.isAmharic()) "የእረፍት ሰዓት" else "Break"
    val resumeWorkButton: String get() = if (LanguageManager.isAmharic()) "ከእረፍት ወደ ሥራ ተመለስ" else "RESUME WORK FROM BREAK"
    val tagFieldVisitButton: String get() = if (LanguageManager.isAmharic()) "የደንበኛ / የመስክ ሥራ መዝግብ (በጂፒኤስ)" else "Tag Client / Field Visit (GPS Stamped)"

    // Activity Timeline
    val todaysActivity: String get() = if (LanguageManager.isAmharic()) "የዛሬ እንቅስቃሴዎች" else "Today's Activity"
    fun eventsCount(count: Int): String = if (LanguageManager.isAmharic()) "$count ክስተቶች" else "$count events"
    val noActivityToday: String get() = if (LanguageManager.isAmharic()) "ዛሬ የተመዘገበ የሥራ እንቅስቃሴ የለም" else "No attendance activity recorded today"
    val tapToClockIn: String get() = if (LanguageManager.isAmharic()) "መግቢያ ለመመዝገብ ከላይ ያለውን አዝራር ይጫኑ" else "Press Clock-In to register your arrival"

    // Record types in Amharic
    val typeClockIn: String get() = if (LanguageManager.isAmharic()) "ሥራ መግቢያ" else "Clock In"
    val typeClockOut: String get() = if (LanguageManager.isAmharic()) "ሥራ መውጫ" else "Clock Out"
    val typeBreakStart: String get() = if (LanguageManager.isAmharic()) "እረፍት ጀመረ" else "Break Started"
    val typeBreakEnd: String get() = if (LanguageManager.isAmharic()) "እረፍት ጨረሰ" else "Break Ended"
    val typeFieldVisit: String get() = if (LanguageManager.isAmharic()) "የመስክ ጉብኝት" else "Field Visit"
    val verifiedOnSite: String get() = if (LanguageManager.isAmharic()) "✓ በቢሮ ውስጥ የተረጋገጠ" else "✓ On-Site Verified"
    val remoteTag: String get() = if (LanguageManager.isAmharic()) "የመስክ / የርቀት ምዝገባ" else "Field / Remote Tag"

    // Branches Screen
    val branchesTitle: String get() = if (LanguageManager.isAmharic()) "የኩባንያው ቅርንጫፎችና የሥራ ክልሎች" else "Company Branches & Work Zones"
    val branchesSubtitle: String get() = if (LanguageManager.isAmharic()) "የሥራ ቦታዎች የጂፒኤስ ክልል (Geofence) የተበጀላቸው ሲሆን የሠራተኞች ሰዓት ምዝገባ በራስ-ሰር ይረጋገጣል።" else "Active geofences define designated office sites across Ethiopia. Attendance clock-ins within each perimeter are verified automatically."
    val configuredSites: String get() = if (LanguageManager.isAmharic()) "የተመዘገቡ ቅርንጫፎች" else "Configured Sites"
    val currentProximity: String get() = if (LanguageManager.isAmharic()) "የአሁኑ መገኛ" else "Current Proximity"
    val onSiteBadge: String get() = if (LanguageManager.isAmharic()) "በቢሮ ውስጥ" else "ON-SITE"
    val remoteBadge: String get() = if (LanguageManager.isAmharic()) "ከቢሮ ውጪ" else "REMOTE"
    val activeSiteBadge: String get() = if (LanguageManager.isAmharic()) "ዋና መገኛ" else "ACTIVE"
    val addBranchTitle: String get() = if (LanguageManager.isAmharic()) "አዲስ ቅርንጫፍ / የሥራ ቦታ መዝግብ" else "Add Company Branch / Site"
    val branchNameInput: String get() = if (LanguageManager.isAmharic()) "የቅርንጫፍ / ቢሮ ስም" else "Branch / Office Name"
    val cityInput: String get() = if (LanguageManager.isAmharic()) "ከተማ / ክልል" else "City / Region"
    val useCurrentGps: String get() = if (LanguageManager.isAmharic()) "የአሁኑን የጂፒኤስ መጋጠሚያ ሙላ" else "Use Current GPS Coordinates"
    fun geofenceRadiusLabel(radius: Int): String = if (LanguageManager.isAmharic()) "የመገኛ ክልል ራዲየስ፡ $radius ሜትር" else "Geofence Radius: $radius meters"
    val addressInput: String get() = if (LanguageManager.isAmharic()) "አድራሻ / አካባቢ መለያ" else "Address / Landmark"
    val saveBranch: String get() = if (LanguageManager.isAmharic()) "ቅርንጫፍ መዝግብ" else "Save Branch"
    val cancel: String get() = if (LanguageManager.isAmharic()) "ተመለስ" else "Cancel"
    val close: String get() = if (LanguageManager.isAmharic()) "ዝጋ" else "Close"

    // History Screen
    val timesheetTitle: String get() = if (LanguageManager.isAmharic()) "የሥራ ሰዓትና ክትትል ታሪክ" else "Attendance Timesheet"
    val timesheetSubtitle: String get() = if (LanguageManager.isAmharic()) "በጂፒኤስ የተረጋገጡ የቀንና ሰዓት ምዝገባዎች" else "Historical GPS-verified punch records"
    val statClockIns: String get() = if (LanguageManager.isAmharic()) "የመግቢያ ምዝገባ" else "Clock-Ins"
    val statFieldVisits: String get() = if (LanguageManager.isAmharic()) "የመስክ ሥራዎች" else "Field Visits"
    val statOnSiteRate: String get() = if (LanguageManager.isAmharic()) "በቢሮ የተገኘበት %" else "On-Site Rate"
    val filterAll: String get() = if (LanguageManager.isAmharic()) "ሁሉም" else "All"
    val filterClockIn: String get() = if (LanguageManager.isAmharic()) "መግቢያ" else "Clock-In"
    val filterClockOut: String get() = if (LanguageManager.isAmharic()) "መውጫ" else "Clock-Out"
    val filterField: String get() = if (LanguageManager.isAmharic()) "የመስክ ሥራ" else "Field Visits"
    val filterBreaks: String get() = if (LanguageManager.isAmharic()) "እረፍቶች" else "Breaks"
    val noRecordsFilter: String get() = if (LanguageManager.isAmharic()) "በዚህ ምድብ የተመዘገበ ታሪክ የለም" else "No records match this filter"
    val clearHistoryConfirmTitle: String get() = if (LanguageManager.isAmharic()) "የክትትል ታሪክ ይሰረዝ?" else "Clear Attendance History?"
    val clearHistoryConfirmBody: String get() = if (LanguageManager.isAmharic()) "ይህ እርምጃ የተመዘገቡትን የመግቢያና የመውጫ ሰዓቶች በሙሉ ይሰርዛል።" else "This will permanently delete all local clock-in, clock-out, and field visit logs."
    val deleteAll: String get() = if (LanguageManager.isAmharic()) "ሁሉንም ሰርዝ" else "Delete All"

    // Profile Screen
    val profileTitle: String get() = if (LanguageManager.isAmharic()) "የሠራተኛ መረጃና መታወቂያ" else "Employee Credentials"
    val editButton: String get() = if (LanguageManager.isAmharic()) "አስተካክል" else "Edit"
    val employeeIdLabel: String get() = if (LanguageManager.isAmharic()) "የሠራተኛ መለያ ቁጥር" else "EMPLOYEE ID"
    val assignedOfficeLabel: String get() = if (LanguageManager.isAmharic()) "የተመደበበት ቢሮ" else "ASSIGNED OFFICE"
    val workDetailsHeader: String get() = if (LanguageManager.isAmharic()) "የሥራ ዝርዝር መረጃ" else "Work Details"
    val policyHeader: String get() = if (LanguageManager.isAmharic()) "የኢትዮጵያ የሥራ ሰዓት ደንብ" else "Ethiopian Work Schedule Policy"
    val policyRule1: String get() = if (LanguageManager.isAmharic()) "• መደበኛ የሥራ ሰዓት፡ ከሰኞ እስከ ዓርብ ከጧቱ 2:30 እስከ ከሰዓት 11:30" else "• Regular Work Hours: Monday – Friday 8:30 AM to 5:30 PM (ጧት 2:30 – ማታ 11:30)"
    val policyRule2: String get() = if (LanguageManager.isAmharic()) "• የምሳ እረፍት ሰዓት፡ ከቀኑ 6:30 እስከ 7:30" else "• Lunch Recess: 12:30 PM to 1:30 PM (ከሰዓት 6:30 – 7:30)"
    val policyRule3: String get() = if (LanguageManager.isAmharic()) "• የቅዳሜ ግማሽ ቀን፡ ከጧቱ 2:30 እስከ 6:30" else "• Saturday Shift: 8:30 AM to 12:30 PM (Half-Day)"
    val policyRule4: String get() = if (LanguageManager.isAmharic()) "• ሰዓት አክባሪነት፡ ከጧቱ 2:30 በኋላ መድረስ እንደ ማዘግየት ይመዘገባል" else "• Punctuality: Arrival after 8:30 AM is recorded as Late arrival."
    val bypassLabel: String get() = if (LanguageManager.isAmharic()) "የመስክ ሠራተኛ / የጂፒኤስ ክልል ማለፊያ" else "Testing: Field Agent / Geofence Bypass"
    val bypassDesc: String get() = if (LanguageManager.isAmharic()) "ከቢሮ ውጪ ሆነው ሲሞክሩ በቢሮ ውስጥ እንዳሉ ሆኖ እንዲመዘገብ ይፈቅዳል።" else "Allows clocking in as On-Site even when testing remotely outside company branch perimeter."

    // Field Visit Dialog
    val fieldVisitDialogTitle: String get() = if (LanguageManager.isAmharic()) "የደንበኛ / የመስክ ጉብኝት መመዝገቢያ" else "Tag Client / Field Visit"
    val fieldVisitDialogDesc: String get() = if (LanguageManager.isAmharic()) "ያሉበት ትክክለኛ የጂፒኤስ መጋጠሚያ ለክትትል ተጠቃሎ ይመዘገባል።" else "Current GPS coordinates will be permanently stamped to verify your on-site client meeting."
    val clientNameLabel: String get() = if (LanguageManager.isAmharic()) "የደንበኛው ወይም የሳይቱ ስም" else "Client or Site Name"
    val clientNamePlaceholder: String get() = if (LanguageManager.isAmharic()) "ለምሳሌ፡ የኢትዮጵያ ንግድ ባንክ ዋና መ/ቤት" else "e.g. Commercial Bank of Ethiopia HQ"
    val fieldNotesLabel: String get() = if (LanguageManager.isAmharic()) "የሥራው ዓላማ / ማስታወሻ" else "Purpose / Activity Notes"
    val fieldNotesPlaceholder: String get() = if (LanguageManager.isAmharic()) "ለምሳሌ፡ የኔትወርክ ሲስተም ጥገና" else "e.g. Server maintenance inspection"
    val stampVisitButton: String get() = if (LanguageManager.isAmharic()) "ጉብኝቱን መዝግብ" else "Stamp Visit"

    // Edit Profile Dialog
    val editProfileTitle: String get() = if (LanguageManager.isAmharic()) "የሠራተኛውን መረጃ አድስ" else "Edit Employee Profile"
    val fullNameLabel: String get() = if (LanguageManager.isAmharic()) "ሙሉ ስም" else "Full Name"
    val departmentLabel: String get() = if (LanguageManager.isAmharic()) "የሥራ ክፍል (ዲፓርትመንት)" else "Department"
    val roleLabel: String get() = if (LanguageManager.isAmharic()) "የሥራ መደብ" else "Position / Title"
    val phoneLabel: String get() = if (LanguageManager.isAmharic()) "ስልክ ቁጥር" else "Phone Number"
    val emailLabel: String get() = if (LanguageManager.isAmharic()) "ኢሜይል" else "Email Address"
    val saveProfile: String get() = if (LanguageManager.isAmharic()) "አስቀምጥ" else "Save"

    // Permission Banner
    val locationPermRequired: String get() = if (LanguageManager.isAmharic()) "የመገኛ ቦታ (ጂፒኤስ) ፈቃድ ያስፈልጋል" else "Location Permission Required"
    val locationPermDesc: String get() = if (LanguageManager.isAmharic()) "በኩባንያው ቅጥር ግቢ ውስጥ መገኘትዎን ለማረጋገጥ የጂፒኤስ ፈቃድ ይስጡ።" else "Real-time location tagging verifies you are within company premises."
    val grantPermButton: String get() = if (LanguageManager.isAmharic()) "ፈቃድ ስጥ" else "Grant"
}
