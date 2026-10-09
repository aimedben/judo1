package com.example.data.repository

import com.example.data.local.AssignmentDao
import com.example.data.local.AttendanceDao
import com.example.data.local.CategoryDao
import com.example.data.local.CoachDao
import com.example.data.local.MemberDao
import com.example.data.local.SessionDao
import com.example.data.model.Attendance
import com.example.data.model.AttendanceWithMember
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CheckInResult {
    data class Success(val member: Member, val attendance: Attendance) : CheckInResult()
    data class AlreadyCheckedIn(val member: Member, val existingAttendance: Attendance) : CheckInResult()
    data class UnknownQr(val rawCode: String) : CheckInResult()
    data class Error(val message: String) : CheckInResult()
}

data class MemberAttendanceStat(
    val member: Member,
    val totalAttendances: Int,
    val attendancePercentage: Int, // e.g. 98%
    val rank: Int = 0
)

class JudoRepository(
    private val memberDao: MemberDao,
    private val attendanceDao: AttendanceDao,
    private val coachDao: CoachDao,
    private val categoryDao: CategoryDao,
    private val assignmentDao: AssignmentDao,
    private val sessionDao: SessionDao
) {
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()
    val activeMembersCount: Flow<Int> = memberDao.countActiveMembersFlow()
    val allAttendance: Flow<List<AttendanceWithMember>> = attendanceDao.getAllAttendanceWithMembers()
    val recentAttendance: Flow<List<AttendanceWithMember>> = attendanceDao.getRecentAttendance(15)

    val allCoaches: Flow<List<Coach>> = coachDao.getAllCoaches()
    val activeCoaches: Flow<List<Coach>> = coachDao.getActiveCoaches()
    val activeCoachesCount: Flow<Int> = coachDao.countActiveCoaches()

    val allCategories: Flow<List<ClubCategory>> = categoryDao.getAllCategories()
    val activeCategories: Flow<List<ClubCategory>> = categoryDao.getActiveCategories()
    val activeCategoriesCount: Flow<Int> = categoryDao.countActiveCategories()

    val allAssignments: Flow<List<CoachCategoryAssignment>> = assignmentDao.getAllAssignments()

    val allSessions: Flow<List<TrainingSession>> = sessionDao.getAllSessions()
    val recentCompletedSessions: Flow<List<TrainingSession>> = sessionDao.getRecentCompletedSessions(10)
    val completedSessionsCount: Flow<Int> = sessionDao.countCompletedSessions()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getTodayAttendance(): Flow<List<AttendanceWithMember>> {
        return attendanceDao.getAttendanceForDate(getTodayDateString())
    }

    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceWithMember>> {
        return attendanceDao.getAttendanceForDate(dateString)
    }

    fun getAttendanceForMember(memberId: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForMember(memberId)
    }

    fun getMemberById(memberId: Long): Flow<Member?> {
        return memberDao.getMemberById(memberId)
    }

    suspend fun getMemberByQr(qrCode: String): Member? {
        return memberDao.getMemberByQrCode(qrCode.trim())
    }

    suspend fun processCheckIn(rawCode: String, method: String = "QR_SCAN"): CheckInResult {
        val cleanCode = rawCode.trim()
        var member = memberDao.getMemberByQrCode(cleanCode)

        if (member == null) {
            val extracted = com.example.util.CardDataParser.parseCardData(cleanCode)
            if (extracted.qrCode.isNotBlank()) {
                member = memberDao.getMemberByQrCode(extracted.qrCode)
            }
            if (member == null && extracted.cardNumber.isNotBlank()) {
                member = memberDao.getMemberByQrCode(extracted.cardNumber)
                    ?: memberDao.getMemberByQrCode("JCS-${extracted.cardNumber}")
            }
        }

        if (member == null) {
            return CheckInResult.UnknownQr(cleanCode)
        }

        val today = getTodayDateString()
        val existing = attendanceDao.findTodayAttendance(member.id, today)
        if (existing != null) {
            return CheckInResult.AlreadyCheckedIn(member, existing)
        }

        val nowTime = getCurrentTimeString()
        val attendance = Attendance(
            memberId = member.id,
            dateString = today,
            timeString = nowTime,
            timestamp = System.currentTimeMillis(),
            checkInMethod = method
        )
        val insertedId = attendanceDao.insertAttendance(attendance)
        return CheckInResult.Success(member, attendance.copy(id = insertedId))
    }

    suspend fun manualCheckIn(memberId: Long): CheckInResult {
        val member = memberDao.getMemberByIdDirect(memberId) ?: return CheckInResult.Error("Membre introuvable")
        val today = getTodayDateString()
        val existing = attendanceDao.findTodayAttendance(member.id, today)
        if (existing != null) {
            return CheckInResult.AlreadyCheckedIn(member, existing)
        }
        val attendance = Attendance(
            memberId = member.id,
            dateString = today,
            timeString = getCurrentTimeString(),
            timestamp = System.currentTimeMillis(),
            checkInMethod = "MANUAL"
        )
        val insertedId = attendanceDao.insertAttendance(attendance)
        return CheckInResult.Success(member, attendance.copy(id = insertedId))
    }

    suspend fun associateQrCodeToMember(memberId: Long, qrCode: String) {
        memberDao.updateMemberQrCode(memberId, qrCode.trim())
    }

    suspend fun insertMember(member: Member): Long {
        return memberDao.insertMember(member)
    }

    suspend fun updateMember(member: Member) {
        memberDao.updateMember(member)
    }

    suspend fun deleteMember(member: Member) {
        memberDao.deleteMember(member)
    }

    suspend fun deleteAttendance(attendanceId: Long) {
        attendanceDao.deleteAttendanceById(attendanceId)
    }

    suspend fun recordAttendanceForDate(
        memberId: Long,
        dateString: String,
        timeString: String = "18:00"
    ): Attendance {
        val existing = attendanceDao.findTodayAttendance(memberId, dateString)
        if (existing != null) return existing

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            sdf.parse(dateString)
        } catch (_: Exception) {
            null
        }
        val timestamp = date?.time ?: System.currentTimeMillis()

        val att = Attendance(
            memberId = memberId,
            dateString = dateString,
            timeString = timeString,
            timestamp = timestamp,
            checkInMethod = "CALENDAR"
        )
        val id = attendanceDao.insertAttendance(att)
        return att.copy(id = id)
    }

    suspend fun deleteAttendanceForMemberAndDate(memberId: Long, dateString: String) {
        val existing = attendanceDao.findTodayAttendance(memberId, dateString)
        if (existing != null) {
            attendanceDao.deleteAttendanceById(existing.id)
        }
    }

    // Coaches operations
    suspend fun insertCoach(coach: Coach): Long = coachDao.insertCoach(coach)
    suspend fun updateCoach(coach: Coach) = coachDao.updateCoach(coach)
    suspend fun deleteCoach(coach: Coach) {
        assignmentDao.deleteAssignmentsForCoach(coach.id)
        coachDao.deleteCoach(coach)
    }

    // Categories operations
    suspend fun insertCategory(category: ClubCategory) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: ClubCategory) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: ClubCategory) = categoryDao.deleteCategory(category)

    // Assignments operations
    suspend fun toggleAssignment(coachId: Long, categoryCode: String, assign: Boolean) {
        if (assign) {
            assignmentDao.insertAssignment(CoachCategoryAssignment(coachId, categoryCode))
        } else {
            assignmentDao.deleteAssignment(coachId, categoryCode)
        }
    }

    // Sessions operations
    suspend fun insertSession(session: TrainingSession): Long = sessionDao.insertSession(session)
    suspend fun updateSession(session: TrainingSession) = sessionDao.updateSession(session)
    suspend fun deleteSession(id: Long) = sessionDao.deleteSessionById(id)
    suspend fun getActiveSession(): TrainingSession? = sessionDao.getActiveSession()

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        try {
            // 1. Seed Members
        if (memberDao.countMembers() == 0) {
            val initialMembers = listOf(
                Member(
                    qrCode = "JS-2026-001",
                    firstName = "Aimed",
                    lastName = "Bensaci",
                    birthDate = "2012-04-12",
                    phone = "0555 12 34 56",
                    groupCode = "U15",
                    beltName = "VERTE",
                    status = "ACTIF",
                    registrationDate = "2024-09-01",
                    notes = "Assidu, très bon morote seoi-nage"
                ),
                Member(
                    qrCode = "JS-2026-002",
                    firstName = "Mohamed",
                    lastName = "Haddad",
                    birthDate = "2011-08-25",
                    phone = "0661 78 90 12",
                    groupCode = "U15",
                    beltName = "BLEUE",
                    status = "ACTIF",
                    registrationDate = "2023-09-10",
                    notes = "Capitaine adjoint Minimes"
                ),
                Member(
                    qrCode = "JS-2026-003",
                    firstName = "Yanis",
                    lastName = "Amroune",
                    birthDate = "2013-01-19",
                    phone = "0770 45 67 89",
                    groupCode = "U13",
                    beltName = "ORANGE",
                    status = "ACTIF",
                    registrationDate = "2025-01-15"
                ),
                Member(
                    qrCode = "JS-2026-004",
                    firstName = "Massinissa",
                    lastName = "Said",
                    birthDate = "2010-11-03",
                    phone = "0550 99 88 77",
                    groupCode = "U18",
                    beltName = "MARRON",
                    status = "ACTIF",
                    registrationDate = "2022-09-01"
                ),
                Member(
                    qrCode = "JS-2026-005",
                    firstName = "Lina",
                    lastName = "Kaci",
                    birthDate = "2014-06-30",
                    phone = "0666 33 22 11",
                    groupCode = "U13",
                    beltName = "JAUNE_ORANGE",
                    status = "ACTIF",
                    registrationDate = "2025-09-01"
                ),
                Member(
                    qrCode = "JS-2026-006",
                    firstName = "Sofiane",
                    lastName = "Meziani",
                    birthDate = "2015-09-14",
                    phone = "0772 11 44 55",
                    groupCode = "U11",
                    beltName = "JAUNE",
                    status = "ACTIF",
                    registrationDate = "2025-10-01"
                ),
                Member(
                    qrCode = "JS-2026-007",
                    firstName = "Rayan",
                    lastName = "Benali",
                    birthDate = "2016-03-22",
                    phone = "0558 77 66 55",
                    groupCode = "U11",
                    beltName = "BLANCHE_JAUNE",
                    status = "ACTIF",
                    registrationDate = "2026-01-10"
                ),
                Member(
                    qrCode = "JS-2026-008",
                    firstName = "Ines",
                    lastName = "Brahimi",
                    birthDate = "2012-07-08",
                    phone = "0664 22 88 99",
                    groupCode = "U15",
                    beltName = "VERTE",
                    status = "ACTIF",
                    registrationDate = "2024-10-05"
                ),
                Member(
                    qrCode = "JS-2026-009",
                    firstName = "Amine",
                    lastName = "Cherif",
                    birthDate = "2008-02-17",
                    phone = "0775 88 11 22",
                    groupCode = "SENIOR",
                    beltName = "NOIRE",
                    status = "ACTIF",
                    registrationDate = "2020-09-01",
                    notes = "Entraîneur bénévole & compétiteur"
                ),
                Member(
                    qrCode = "JS-2026-010",
                    firstName = "Celia",
                    lastName = "Hamadi",
                    birthDate = "2017-12-05",
                    phone = "0553 44 77 11",
                    groupCode = "U9",
                    beltName = "BLANCHE",
                    status = "ACTIF",
                    registrationDate = "2026-09-01"
                ),
                Member(
                    qrCode = "JS-2026-011",
                    firstName = "Bilal",
                    lastName = "Tigrine",
                    birthDate = "2011-05-18",
                    phone = "0661 44 33 22",
                    groupCode = "U15",
                    beltName = "BLEUE",
                    status = "ACTIF",
                    registrationDate = "2023-09-01"
                ),
                Member(
                    qrCode = "JS-2026-012",
                    firstName = "Kenzi",
                    lastName = "Ait Ouali",
                    birthDate = "2018-04-10",
                    phone = "0771 99 00 22",
                    groupCode = "EVEIL",
                    beltName = "BLANCHE",
                    status = "ACTIF",
                    registrationDate = "2026-09-15"
                )
            )
            memberDao.insertMembers(initialMembers)
        }

        // 2. Seed Categories
        if (categoryDao.countCategoriesDirect() == 0) {
            val categories = listOf(
                ClubCategory(code = "EVEIL", label = "Éveil Judo", ageRange = "4-5 ans", schedule = "Mercredi & Samedi 10h00"),
                ClubCategory(code = "U9", label = "Mini-Poussins (U9)", ageRange = "6-7 ans", schedule = "Mercredi & Samedi 14h00"),
                ClubCategory(code = "U11", label = "Poussins (U11)", ageRange = "8-9 ans", schedule = "Lundi & Jeudi 17h00"),
                ClubCategory(code = "U13", label = "Benjamins (U13)", ageRange = "10-11 ans", schedule = "Mardi & Vendredi 17h30"),
                ClubCategory(code = "U15", label = "Minimes (U15)", ageRange = "12-13 ans", schedule = "Lundi, Mercredi & Vendredi 18h00"),
                ClubCategory(code = "U18", label = "Cadets (U18)", ageRange = "14-16 ans", schedule = "Mardi & Jeudi 19h00"),
                ClubCategory(code = "SENIOR", label = "Juniors / Séniors", ageRange = "17+ ans", schedule = "Lundi, Mercredi & Vendredi 19h30")
            )
            categoryDao.insertCategories(categories)
        }

        // 3. Seed Coaches
        if (coachDao.countCoachesDirect() == 0) {
            val coaches = listOf(
                Coach(
                    id = 1,
                    name = "Sensei Ahmed BOURENANE",
                    title = "Directeur Technique & Entraîneur Principal",
                    beltName = "NOIRE",
                    danGrade = "4ème Dan",
                    phone = "0782 48 71 20",
                    isActive = true
                ),
                Coach(
                    id = 2,
                    name = "Sensei Karim MEZIANI",
                    title = "Entraîneur Adjoint & Préparateur Physique",
                    beltName = "NOIRE",
                    danGrade = "2ème Dan",
                    phone = "0550 12 34 56",
                    isActive = true
                ),
                Coach(
                    id = 3,
                    name = "Sensei Amine CHERIF",
                    title = "Entraîneur Jeunes & Benjamins",
                    beltName = "NOIRE",
                    danGrade = "1er Dan",
                    phone = "0775 88 11 22",
                    isActive = true
                ),
                Coach(
                    id = 4,
                    name = "Sensei Yacine TIGRINE",
                    title = "Assistant Technique & Éveil",
                    beltName = "MARRON",
                    danGrade = "Ceinture Marron",
                    phone = "0661 99 88 77",
                    isActive = true
                )
            )
            coachDao.insertCoaches(coaches)
        }

        // 4. Seed Coach-Category Assignments
        if (assignmentDao.countAssignmentsDirect() == 0) {
            val assignments = listOf(
                CoachCategoryAssignment(coachId = 1, categoryCode = "U15"),
                CoachCategoryAssignment(coachId = 1, categoryCode = "U18"),
                CoachCategoryAssignment(coachId = 1, categoryCode = "SENIOR"),
                CoachCategoryAssignment(coachId = 2, categoryCode = "U13"),
                CoachCategoryAssignment(coachId = 2, categoryCode = "U15"),
                CoachCategoryAssignment(coachId = 3, categoryCode = "U11"),
                CoachCategoryAssignment(coachId = 3, categoryCode = "U9"),
                CoachCategoryAssignment(coachId = 4, categoryCode = "EVEIL"),
                CoachCategoryAssignment(coachId = 4, categoryCode = "U9")
            )
            assignmentDao.insertAssignments(assignments)
        }

        // 5. Seed Training Sessions
        if (sessionDao.countSessionsDirect() == 0) {
            val sessions = listOf(
                TrainingSession(
                    categoryCode = "U15",
                    dateString = getTodayDateString(),
                    startTime = "18:00",
                    endTime = "19:30",
                    durationMinutes = 90,
                    presentCoachNames = "Sensei Ahmed BOURENANE, Sensei Karim MEZIANI",
                    presentCount = 4,
                    totalCategoryMembers = 4,
                    notes = "Séance technique : travail Seoi-Nage et liaisons debout-sol.",
                    status = "COMPLETED"
                ),
                TrainingSession(
                    categoryCode = "U13",
                    dateString = getTodayDateString(),
                    startTime = "17:30",
                    endTime = "18:45",
                    durationMinutes = 75,
                    presentCoachNames = "Sensei Karim MEZIANI",
                    presentCount = 2,
                    totalCategoryMembers = 2,
                    notes = "Randori souple et révisions ceintures.",
                    status = "COMPLETED"
                ),
                TrainingSession(
                    categoryCode = "U11",
                    dateString = "2026-10-06",
                    startTime = "17:00",
                    endTime = "18:15",
                    durationMinutes = 75,
                    presentCoachNames = "Sensei Amine CHERIF",
                    presentCount = 2,
                    totalCategoryMembers = 2,
                    notes = "Jeux d'opposition et chutes.",
                    status = "COMPLETED"
                ),
                TrainingSession(
                    categoryCode = "U18",
                    dateString = "2026-10-05",
                    startTime = "19:00",
                    endTime = "20:30",
                    durationMinutes = 90,
                    presentCoachNames = "Sensei Ahmed BOURENANE",
                    presentCount = 1,
                    totalCategoryMembers = 1,
                    notes = "Préparation compétition de wilaya.",
                    status = "COMPLETED"
                )
            )
            sessionDao.insertSessions(sessions)
        }

        // 6. Seed Attendances if needed
        val insertedMembers = memberDao.countMembers()
        if (insertedMembers > 0 && attendanceDao.countTotalAttendance() < 10) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 3600 * 1000L
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date(now))

            val historicalList = mutableListOf<Attendance>()

            // Today's attendances
            historicalList.add(Attendance(memberId = 1, dateString = today, timeString = "18:01", timestamp = now - 3600_000L, checkInMethod = "QR_SCAN"))
            historicalList.add(Attendance(memberId = 2, dateString = today, timeString = "18:03", timestamp = now - 3400_000L, checkInMethod = "QR_SCAN"))
            historicalList.add(Attendance(memberId = 3, dateString = today, timeString = "18:04", timestamp = now - 3200_000L, checkInMethod = "QR_SCAN"))
            historicalList.add(Attendance(memberId = 4, dateString = today, timeString = "18:07", timestamp = now - 2800_000L, checkInMethod = "QR_SCAN"))
            historicalList.add(Attendance(memberId = 5, dateString = today, timeString = "18:09", timestamp = now - 2500_000L, checkInMethod = "QR_SCAN"))

            val pastDaysOffsets = listOf(
                1L to listOf(1L, 2L, 4L, 6L, 7L, 8L),
                2L to listOf(2L, 3L, 5L, 6L, 9L),
                4L to listOf(1L, 3L, 4L, 7L, 8L, 9L),
                6L to listOf(1L, 2L, 5L, 6L, 10L),
                9L to listOf(2L, 3L, 4L, 7L, 8L, 11L),
                12L to listOf(1L, 2L, 4L, 5L, 6L, 9L),
                16L to listOf(1L, 3L, 4L, 7L, 8L),
                20L to listOf(2L, 4L, 5L, 6L, 9L, 10L),
                24L to listOf(1L, 2L, 3L, 6L, 7L, 8L),
                28L to listOf(1L, 4L, 5L, 8L, 9L),
                35L to listOf(2L, 3L, 4L, 6L, 7L, 11L),
                42L to listOf(1L, 2L, 5L, 8L, 9L),
                49L to listOf(1L, 3L, 4L, 6L, 7L, 10L),
                56L to listOf(2L, 4L, 5L, 8L, 9L, 11L)
            )

            for ((daysAgo, membersAttended) in pastDaysOffsets) {
                val pastTime = now - (daysAgo * dayMs)
                val dateStr = sdf.format(Date(pastTime))
                for (memberId in membersAttended) {
                    val hour = 17 + (memberId.toInt() % 3)
                    val minute = (memberId.toInt() * 7) % 60
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    historicalList.add(
                        Attendance(
                            memberId = memberId,
                            dateString = dateStr,
                            timeString = timeStr,
                            timestamp = pastTime - (hour * 3600_000L),
                            checkInMethod = "QR_SCAN"
                        )
                    )
                }
            }

            attendanceDao.insertAll(historicalList)
        }
        } catch (_: Exception) {}
    }
}
