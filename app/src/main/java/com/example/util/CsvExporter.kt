package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.AttendanceWithMember
import com.example.data.model.Member
import com.example.data.model.TrainingSession

object CsvExporter {

    fun generateAttendanceCsv(
        attendances: List<AttendanceWithMember>,
        title: String = "presences_judo_seddouk"
    ): String {
        val sb = StringBuilder()
        sb.append("Date,Heure,Nom,Prénom,Groupe,Ceinture,Code_QR,Telephone\n")
        attendances.forEach { item ->
            sb.append("\"${item.dateString}\",")
            sb.append("\"${item.timeString}\",")
            sb.append("\"${item.lastName}\",")
            sb.append("\"${item.firstName}\",")
            sb.append("\"${item.groupCode}\",")
            sb.append("\"${item.beltName}\",")
            sb.append("\"${item.qrCode}\",")
            sb.append("\"${item.phone}\"\n")
        }
        return sb.toString()
    }

    fun generateMembersCsv(members: List<Member>): String {
        val sb = StringBuilder()
        sb.append("ID,Nom,Prénom,Date_Naissance,Groupe,Ceinture,Code_QR,Téléphone,Statut,Date_Inscription\n")
        members.forEach { m ->
            sb.append("${m.id},")
            sb.append("\"${m.lastName}\",")
            sb.append("\"${m.firstName}\",")
            sb.append("\"${m.birthDate}\",")
            sb.append("\"${m.groupCode}\",")
            sb.append("\"${m.beltName}\",")
            sb.append("\"${m.qrCode}\",")
            sb.append("\"${m.phone}\",")
            sb.append("\"${m.status}\",")
            sb.append("\"${m.registrationDate}\"\n")
        }
        return sb.toString()
    }

    fun generateSessionsCsv(sessions: List<TrainingSession>): String {
        val sb = StringBuilder()
        sb.append("ID,Catégorie,Date,Début,Fin,Durée_Min,Coachs_Présents,Présents,Total,Taux_Pct,Notes\n")
        sessions.forEach { s ->
            sb.append("${s.id},")
            sb.append("\"${s.categoryCode}\",")
            sb.append("\"${s.dateString}\",")
            sb.append("\"${s.startTime}\",")
            sb.append("\"${s.endTime}\",")
            sb.append("${s.durationMinutes},")
            sb.append("\"${s.presentCoachNames}\",")
            sb.append("${s.presentCount},")
            sb.append("${s.totalCategoryMembers},")
            sb.append("${s.attendanceRate}%,")
            sb.append("\"${s.notes}\"\n")
        }
        return sb.toString()
    }

    fun shareAttendanceReport(
        context: Context,
        attendances: List<AttendanceWithMember>,
        filterLabel: String
    ) {
        val csvData = generateAttendanceCsv(attendances)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Pointage Judo Seddouk - $filterLabel")
            putExtra(
                Intent.EXTRA_TEXT,
                "🥋 Rapport de présence - Judo Seddouk ($filterLabel)\n" +
                "Total pointages: ${attendances.size}\n\n" +
                csvData
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager le rapport de présence"))
    }

    fun shareMembersReport(context: Context, members: List<Member>) {
        val csvData = generateMembersCsv(members)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Liste des Judokas - Judo Seddouk")
            putExtra(
                Intent.EXTRA_TEXT,
                "🥋 Registre des Judokas - Judo Seddouk\n" +
                "Total inscrits: ${members.size}\n\n" +
                csvData
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager le registre des membres"))
    }

    fun shareSessionsReport(context: Context, sessions: List<TrainingSession>) {
        val csvData = generateSessionsCsv(sessions)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Rapport des Séances d'Entraînement - Judo Seddouk")
            putExtra(
                Intent.EXTRA_TEXT,
                "🥋 Historique des Séances - Judo Seddouk\n" +
                "Total séances: ${sessions.size}\n\n" +
                csvData
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager l'historique des séances"))
    }
}
