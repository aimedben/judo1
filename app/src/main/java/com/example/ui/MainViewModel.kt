package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceWithMember
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.CoachProfile
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import com.example.data.repository.CheckInResult
import com.example.data.repository.JudoRepository
import com.example.data.repository.MemberAttendanceStat
import com.example.ui.screens.StatPeriod
import com.example.util.CardDataParser
import com.example.util.ExtractedMemberInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ThemePreference {
    DARK, LIGHT, SYSTEM
}

enum class UserRole {
    ADMIN, COACH
}

data class ActiveSessionState(
    val categoryCode: String,
    val categoryLabel: String,
    val dateString: String,
    val startTime: String,
    val startTimestamp: Long,
    val presentCoachNames: List<String>,
    val checkedInMemberIds: Set<Long> = emptySet(),
    val checkInTimes: Map<Long, String> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("judo_seddouk_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getInstance(application)
    private val repository = JudoRepository(
        database.memberDao(),
        database.attendanceDao(),
        database.coachDao(),
        database.categoryDao(),
        database.assignmentDao(),
        database.sessionDao()
    )

    // User Role
    private val _userRole = MutableStateFlow(
        try {
            UserRole.valueOf(prefs.getString("user_role", UserRole.ADMIN.name) ?: UserRole.ADMIN.name)
        } catch (_: Exception) {
            UserRole.ADMIN
        }
    )
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    fun setUserRole(role: UserRole) {
        _userRole.value = role
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun toggleUserRole() {
        val next = if (_userRole.value == UserRole.ADMIN) UserRole.COACH else UserRole.ADMIN
        setUserRole(next)
    }

    // Active selected coach for Coach Mode
    private val _selectedCoachId = MutableStateFlow(prefs.getLong("selected_coach_id", 1L))
    val selectedCoachId: StateFlow<Long> = _selectedCoachId.asStateFlow()

    fun setSelectedCoachId(coachId: Long) {
        _selectedCoachId.value = coachId
        prefs.edit().putLong("selected_coach_id", coachId).apply()
    }

    // Theme
    private val _themePreference = MutableStateFlow(
        try {
            ThemePreference.valueOf(prefs.getString("theme_pref", ThemePreference.DARK.name) ?: ThemePreference.DARK.name)
        } catch (_: Exception) {
            ThemePreference.DARK
        }
    )
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    fun setThemePreference(pref: ThemePreference) {
        _themePreference.value = pref
        prefs.edit().putString("theme_pref", pref.name).apply()
    }

    fun toggleTheme() {
        val next = if (_themePreference.value == ThemePreference.DARK) ThemePreference.LIGHT else ThemePreference.DARK
        setThemePreference(next)
    }

    // Coach Profile (Legacy Sensei Profile dialog)
    private val _coachProfile = MutableStateFlow(loadCoachProfile())
    val coachProfile: StateFlow<CoachProfile> = _coachProfile.asStateFlow()

    private fun loadCoachProfile(): CoachProfile {
        return CoachProfile(
            name = prefs.getString("coach_name", "Sensei Ahmed BOURENANE") ?: "Sensei Ahmed BOURENANE",
            title = prefs.getString("coach_title", "Directeur Technique & Entraîneur Principal") ?: "Directeur Technique & Entraîneur Principal",
            beltName = prefs.getString("coach_belt", "NOIRE") ?: "NOIRE",
            danGrade = prefs.getString("coach_dan", "Ceinture Noire 4ème Dan") ?: "Ceinture Noire 4ème Dan",
            phone = prefs.getString("coach_phone", "0782 48 71 20") ?: "0782 48 71 20",
            photoUrl = prefs.getString("coach_photo_url", "") ?: "",
            clubName = prefs.getString("coach_club", "JUDO CLUB SEDDOUK") ?: "JUDO CLUB SEDDOUK",
            bio = prefs.getString("coach_bio", "Professeur diplômé d'État • Dojo Municipal de Seddouk") ?: "Professeur diplômé d'État • Dojo Municipal de Seddouk"
        )
    }

    fun updateCoachProfile(profile: CoachProfile) {
        _coachProfile.value = profile
        prefs.edit()
            .putString("coach_name", profile.name)
            .putString("coach_title", profile.title)
            .putString("coach_belt", profile.beltName)
            .putString("coach_dan", profile.danGrade)
            .putString("coach_phone", profile.phone)
            .putString("coach_photo_url", profile.photoUrl)
            .putString("coach_club", profile.clubName)
            .putString("coach_bio", profile.bio)
            .apply()
    }

    // Database flows
    val allMembers: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<List<AttendanceWithMember>> = repository.getTodayAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAttendance: StateFlow<List<AttendanceWithMember>> = repository.recentAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceWithMember>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoaches: StateFlow<List<Coach>> = repository.allCoaches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCoaches: StateFlow<List<Coach>> = repository.activeCoaches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<ClubCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<CoachCategoryAssignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<TrainingSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCompletedSessions: StateFlow<List<TrainingSession>> = repository.recentCompletedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session state (for Coach live training workflow)
    private val _activeSession = MutableStateFlow<ActiveSessionState?>(null)
    val activeSession: StateFlow<ActiveSessionState?> = _activeSession.asStateFlow()

    // Completed session summary state (shows end-of-session screen)
    private val _completedSessionSummary = MutableStateFlow<TrainingSession?>(null)
    val completedSessionSummary: StateFlow<TrainingSession?> = _completedSessionSummary.asStateFlow()

    // Check-in and scanning results
    private val _lastScanned = MutableStateFlow<AttendanceWithMember?>(null)
    val lastScanned: StateFlow<AttendanceWithMember?> = _lastScanned.asStateFlow()

    private val _activeCheckInResult = MutableStateFlow<CheckInResult?>(null)
    val activeCheckInResult: StateFlow<CheckInResult?> = _activeCheckInResult.asStateFlow()

    private val _unassignedQrCode = MutableStateFlow<String?>(null)
    val unassignedQrCode: StateFlow<String?> = _unassignedQrCode.asStateFlow()

    private val _scannedMemberForCreation = MutableStateFlow<ExtractedMemberInfo?>(null)
    val scannedMemberForCreation: StateFlow<ExtractedMemberInfo?> = _scannedMemberForCreation.asStateFlow()

    private var lastScannedCode: String = ""
    private var lastScannedTimestamp: Long = 0L

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Training Session Management ---

    fun startNewSession(
        categoryCode: String,
        categoryLabel: String,
        presentCoachNames: List<String>
    ) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date(now))
        val nowTime = timeFormat.format(Date(now))

        // Pre-fill already checked-in members today for this category if any
        val categoryMembers = allMembers.value.filter { it.groupCode.equals(categoryCode, ignoreCase = true) }
        val todayCategoryAtt = todayAttendance.value.filter { it.groupCode.equals(categoryCode, ignoreCase = true) }
        val checkedInIds = todayCategoryAtt.map { it.memberId }.toSet()
        val timesMap = todayCategoryAtt.associate { it.memberId to it.timeString }

        _activeSession.value = ActiveSessionState(
            categoryCode = categoryCode,
            categoryLabel = categoryLabel,
            dateString = todayStr,
            startTime = nowTime,
            startTimestamp = now,
            presentCoachNames = presentCoachNames,
            checkedInMemberIds = checkedInIds,
            checkInTimes = timesMap
        )
    }

    fun completeSession(notes: String = "") {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val endTime = timeFormat.format(Date(now))
        val durationMins = ((now - session.startTimestamp) / (60 * 1000L)).toInt().coerceAtLeast(1)

        val totalCategoryMembers = allMembers.value.count {
            it.groupCode.equals(session.categoryCode, ignoreCase = true) && it.status == "ACTIF"
        }

        val completed = TrainingSession(
            categoryCode = session.categoryCode,
            dateString = session.dateString,
            startTime = session.startTime,
            endTime = endTime,
            durationMinutes = durationMins,
            presentCoachNames = session.presentCoachNames.joinToString(", "),
            presentCount = session.checkedInMemberIds.size,
            totalCategoryMembers = totalCategoryMembers.coerceAtLeast(session.checkedInMemberIds.size),
            notes = notes,
            status = "COMPLETED"
        )

        viewModelScope.launch {
            val id = repository.insertSession(completed)
            _completedSessionSummary.value = completed.copy(id = id)
            _activeSession.value = null
        }
    }

    fun dismissCompletedSessionSummary() {
        _completedSessionSummary.value = null
    }

    fun cancelActiveSession() {
        _activeSession.value = null
    }

    // --- Check-in Logic ---

    fun onQrScanned(code: String) {
        val now = System.currentTimeMillis()
        val clean = code.trim()
        if (clean.isBlank()) return

        // Debounce exact duplicate scan within 2.5 seconds
        if (clean == lastScannedCode && (now - lastScannedTimestamp) < 2500) {
            return
        }
        lastScannedCode = clean
        lastScannedTimestamp = now

        viewModelScope.launch {
            val result = repository.processCheckIn(clean)
            triggerHapticFeedback(result)

            if (result is CheckInResult.Success) {
                // If an active session is in progress, mark this member as present in the active session
                val current = _activeSession.value
                if (current != null) {
                    val updatedIds = current.checkedInMemberIds + result.member.id
                    val updatedTimes = current.checkInTimes + (result.member.id to result.attendance.timeString)
                    _activeSession.value = current.copy(
                        checkedInMemberIds = updatedIds,
                        checkInTimes = updatedTimes
                    )
                }

                _lastScanned.value = AttendanceWithMember(
                    attendanceId = result.attendance.id,
                    memberId = result.member.id,
                    firstName = result.member.firstName,
                    lastName = result.member.lastName,
                    groupCode = result.member.groupCode,
                    beltName = result.member.beltName,
                    dateString = result.attendance.dateString,
                    timeString = result.attendance.timeString,
                    timestamp = result.attendance.timestamp,
                    qrCode = result.member.qrCode,
                    phone = result.member.phone
                )
            }

            when (result) {
                is CheckInResult.Success, is CheckInResult.AlreadyCheckedIn -> {
                    _activeCheckInResult.value = result
                }
                is CheckInResult.UnknownQr -> {
                    val extracted = CardDataParser.parseCardData(clean)
                    if (extracted.firstName.isNotBlank() || extracted.lastName.isNotBlank()) {
                        _scannedMemberForCreation.value = extracted
                    } else {
                        _unassignedQrCode.value = clean
                    }
                    _activeCheckInResult.value = result
                }
                is CheckInResult.Error -> {
                    _activeCheckInResult.value = result
                }
            }
        }
    }

    fun manualCheckIn(memberId: Long) {
        viewModelScope.launch {
            val result = repository.manualCheckIn(memberId)
            triggerHapticFeedback(result)
            _activeCheckInResult.value = result

            if (result is CheckInResult.Success) {
                val current = _activeSession.value
                if (current != null) {
                    val updatedIds = current.checkedInMemberIds + result.member.id
                    val updatedTimes = current.checkInTimes + (result.member.id to result.attendance.timeString)
                    _activeSession.value = current.copy(
                        checkedInMemberIds = updatedIds,
                        checkInTimes = updatedTimes
                    )
                }

                _lastScanned.value = AttendanceWithMember(
                    attendanceId = result.attendance.id,
                    memberId = result.member.id,
                    firstName = result.member.firstName,
                    lastName = result.member.lastName,
                    groupCode = result.member.groupCode,
                    beltName = result.member.beltName,
                    dateString = result.attendance.dateString,
                    timeString = result.attendance.timeString,
                    timestamp = result.attendance.timestamp,
                    qrCode = result.member.qrCode,
                    phone = result.member.phone
                )
            }
        }
    }

    fun extractCardDataForMemberCreation(rawCardData: String) {
        val extracted = CardDataParser.parseCardData(rawCardData)
        _scannedMemberForCreation.value = extracted
    }

    fun clearScannedMemberForCreation() {
        _scannedMemberForCreation.value = null
    }

    fun recordAttendanceForDate(memberId: Long, dateString: String) {
        viewModelScope.launch {
            repository.recordAttendanceForDate(memberId, dateString)
        }
    }

    fun deleteAttendanceForDate(memberId: Long, dateString: String) {
        viewModelScope.launch {
            repository.deleteAttendanceForMemberAndDate(memberId, dateString)
        }
    }

    fun associateQrToMember(memberId: Long, qrCode: String) {
        viewModelScope.launch {
            repository.associateQrCodeToMember(memberId, qrCode)
            _unassignedQrCode.value = null
            val result = repository.processCheckIn(qrCode)
            _activeCheckInResult.value = result
        }
    }

    fun cancelUnassignedQr() {
        _unassignedQrCode.value = null
        if (_activeCheckInResult.value is CheckInResult.UnknownQr) {
            _activeCheckInResult.value = null
        }
    }

    fun dismissCheckInDialog() {
        _activeCheckInResult.value = null
    }

    // --- Member CRUD ---

    fun saveMember(member: Member) {
        viewModelScope.launch {
            if (member.id == 0L) {
                repository.insertMember(member)
                repository.processCheckIn(member.qrCode)
            } else {
                repository.updateMember(member)
            }
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun deleteAttendance(attendanceId: Long) {
        viewModelScope.launch {
            repository.deleteAttendance(attendanceId)
        }
    }

    // --- Coach CRUD & Assignments ---

    fun saveCoach(coach: Coach) {
        viewModelScope.launch {
            if (coach.id == 0L) {
                repository.insertCoach(coach)
            } else {
                repository.updateCoach(coach)
            }
        }
    }

    fun deleteCoach(coach: Coach) {
        viewModelScope.launch {
            repository.deleteCoach(coach)
        }
    }

    fun toggleCoachAssignment(coachId: Long, categoryCode: String, assign: Boolean) {
        viewModelScope.launch {
            repository.toggleAssignment(coachId, categoryCode, assign)
        }
    }

    // --- Category CRUD ---

    fun saveCategory(category: ClubCategory) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: ClubCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // --- Stats & Ranking Helpers ---

    fun filterAttendancesByPeriod(
        attendances: List<AttendanceWithMember>,
        period: StatPeriod
    ): List<AttendanceWithMember> {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 3600 * 1000L
        val todayStr = repository.getTodayDateString()

        return when (period) {
            StatPeriod.TODAY -> attendances.filter { it.dateString == todayStr }
            StatPeriod.WEEK -> {
                val cutoff = now - (7L * dayMs)
                attendances.filter { it.timestamp >= cutoff }
            }
            StatPeriod.MONTH -> {
                val cutoff = now - (30L * dayMs)
                attendances.filter { it.timestamp >= cutoff }
            }
            StatPeriod.TWO_MONTHS -> {
                val cutoff = now - (60L * dayMs)
                attendances.filter { it.timestamp >= cutoff }
            }
            StatPeriod.SEASON -> attendances
        }
    }

    fun getTopAttendanceList(
        allMembersList: List<Member>,
        allAttendances: List<AttendanceWithMember>,
        period: StatPeriod = StatPeriod.SEASON
    ): List<MemberAttendanceStat> {
        if (allMembersList.isEmpty()) return emptyList()
        val periodAttendances = filterAttendancesByPeriod(allAttendances, period)
        val attendanceCountByMember = periodAttendances.groupBy { it.memberId }
            .mapValues { it.value.size }

        val maxAttendance = attendanceCountByMember.values.maxOrNull() ?: 1
        val baselineTotal = when (period) {
            StatPeriod.TODAY -> 1
            StatPeriod.WEEK -> 3.coerceAtLeast(maxAttendance)
            StatPeriod.MONTH -> 10.coerceAtLeast(maxAttendance)
            StatPeriod.TWO_MONTHS -> 18.coerceAtLeast(maxAttendance)
            StatPeriod.SEASON -> 30.coerceAtLeast(maxAttendance)
        }

        return allMembersList.map { member ->
            val count = attendanceCountByMember[member.id] ?: 0
            val pct = if (count == 0) 0 else ((count.toDouble() / baselineTotal.toDouble()) * 100).toInt().coerceIn(1, 100)
            MemberAttendanceStat(
                member = member,
                totalAttendances = count,
                attendancePercentage = pct
            )
        }.sortedWith(
            compareByDescending<MemberAttendanceStat> { it.totalAttendances }
                .thenByDescending { it.attendancePercentage }
        )
    }

    private fun triggerHapticFeedback(result: CheckInResult) {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = when (result) {
                    is CheckInResult.Success -> longArrayOf(0, 70, 50, 90)
                    is CheckInResult.AlreadyCheckedIn -> longArrayOf(0, 150)
                    else -> longArrayOf(0, 100, 100, 100)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }
}
