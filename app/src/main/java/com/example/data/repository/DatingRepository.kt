package com.example.data.repository

import com.example.data.dao.DatingDao
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.RealDate
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DatingRepository(private val dao: DatingDao) {

    val feedProfiles: Flow<List<DatingProfile>> = dao.getFeedProfiles()
    val matchedProfiles: Flow<List<DatingProfile>> = dao.getMatchedProfiles()
    val allDates: Flow<List<RealDate>> = dao.getAllDates()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    fun getProfile(id: Long): Flow<DatingProfile?> = dao.getProfileById(id)

    fun getMessages(profileId: Long): Flow<List<ChatMessage>> = dao.getMessagesForProfile(profileId)

    suspend fun matchWithProfile(id: Long) {
        dao.matchProfile(id)
        // Add an initial greeting from the match
        val profile = dao.getProfileById(id).first()
        if (profile != null) {
            dao.insertMessage(
                ChatMessage(
                    profileId = id,
                    senderName = profile.name,
                    messageText = "¡Hola! Vi que te gustó mi plan para '${profile.idealDateTitle}'. ¡Me encantaría que lo hiciéramos realidad! ¿Qué día te vendría bien?",
                    isFromUser = false,
                    isDateProposal = true
                )
            )
        }
    }

    suspend fun passProfile(id: Long) {
        dao.passProfile(id)
    }

    suspend fun resetFeed() {
        dao.resetAllProfiles()
    }

    suspend fun proposeRealDate(
        profileId: Long,
        profileName: String,
        profileDrawable: String,
        title: String,
        location: String,
        dateTimeString: String,
        category: String,
        notes: String
    ): Long {
        val date = RealDate(
            profileId = profileId,
            profileName = profileName,
            profileDrawable = profileDrawable,
            title = title,
            location = location,
            dateTimeString = dateTimeString,
            category = category,
            status = "CONFIRMADA",
            notes = notes,
            isSafetyProtocolActive = true
        )
        val dateId = dao.insertDate(date)

        // Send a confirmation chat message
        dao.insertMessage(
            ChatMessage(
                profileId = profileId,
                senderName = "Sistema CitaReal",
                messageText = "¡Cita agendada con éxito para $dateTimeString en $location! Compromiso mutuo de asistencia confirmado.",
                isFromUser = true,
                isDateProposal = true,
                dateProposalId = dateId
            )
        )

        return dateId
    }

    suspend fun updateDateStatus(date: RealDate, newStatus: String) {
        dao.updateDate(date.copy(status = newStatus))
    }

    suspend fun deleteDate(id: Long) {
        dao.deleteDate(id)
    }

    suspend fun sendUserMessage(profileId: Long, text: String, profileName: String) {
        dao.insertMessage(
            ChatMessage(
                profileId = profileId,
                senderName = "Tú",
                messageText = text,
                isFromUser = true
            )
        )
    }

    suspend fun sendMatchReply(profileId: Long, replyText: String, matchName: String) {
        dao.insertMessage(
            ChatMessage(
                profileId = profileId,
                senderName = matchName,
                messageText = replyText,
                isFromUser = false
            )
        )
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dao.insertOrUpdateUserProfile(profile)
    }

    suspend fun recalculateCompatibility(newPreferences: List<String>) {
        // Boost compatibility scores for profiles that share interests or categories
        val feed = dao.getFeedProfiles().first()
        for (item in feed) {
            var bonus = 0
            for (pref in newPreferences) {
                if (item.interests.contains(pref, ignoreCase = true) ||
                    item.idealDateCategory.contains(pref, ignoreCase = true) ||
                    item.nonNegotiables.contains(pref, ignoreCase = true)) {
                    bonus += 5
                }
            }
            val newScore = (item.compatibilityScore + bonus).coerceIn(75, 99)
            dao.updateProfile(item.copy(compatibilityScore = newScore))
        }
    }

    suspend fun ensureInitialData() {
        val count = dao.getProfileCount()
        if (count == 0) {
            val sampleProfiles = listOf(
                DatingProfile(
                    name = "Sofía Valenzuela",
                    age = 27,
                    occupation = "Diseñadora de Espacios",
                    bio = "Cansada de semanas enteras chateando sin vernos. Busco risas reales, café honesto y alguien que no tenga miedo a mirar a los ojos.",
                    city = "Barrio Roma / Centro",
                    distanceKm = 3,
                    verifiedHuman = true,
                    drawableResName = "profile_sofia",
                    idealDateTitle = "Ruta de café de especialidad y charla sin filtros",
                    idealDateDescription = "Probar dos tipos de extracción en una barra tranquila, caminar por el parque y contarnos qué nos apasiona de verdad.",
                    idealDateLocation = "Café La Terraza, Calle Orizaba 42",
                    idealDateCategory = "Café & Charla",
                    interests = "Arquitectura, Fotografía urbana, Café de filtro, Cine club",
                    nonNegotiables = "Honestidad radical, Puntualidad con afecto, Cero pantallas en la mesa",
                    relationshipGoal = "Conexión auténtica a largo plazo",
                    compatibilityScore = 96,
                    icebreakerPrompt = "La señal más clara de que una cita va de maravilla es...",
                    icebreakerAnswer = "Cuando el café se enfría porque la conversación no tiene pausas incómodas."
                ),
                DatingProfile(
                    name = "Mateo Herrera",
                    age = 29,
                    occupation = "Guía de Montaña & Fotógrafo",
                    bio = "La vida real sucede lejos de los algoritmos. Si te gusta el olor a tierra mojada, las historias junto al fuego y la sinceridad, encajaremos.",
                    city = "San Ángel",
                    distanceKm = 5,
                    verifiedHuman = true,
                    drawableResName = "profile_mateo",
                    idealDateTitle = "Atardecer en el mirador con termos y cámaras análogas",
                    idealDateDescription = "Subir a un punto alto antes de las 18:00, llevar café de olla caliente y disfrutar la puesta de sol en silencio o con buena música.",
                    idealDateLocation = "Mirador Panorámico Los Pinos",
                    idealDateCategory = "Aventura al Aire Libre",
                    interests = "Trekking, Fotografía analógica, Acampada, Perros rescatados",
                    nonNegotiables = "Amor por la naturaleza, Cero posturas fingidas, Empatía",
                    relationshipGoal = "Compañera de vida y aventuras",
                    compatibilityScore = 93,
                    icebreakerPrompt = "Mi plan ideal de domingo temprano:",
                    icebreakerAnswer = "Salir a rodar o caminar temprano con el aire fresco y desayunar en el mercado local."
                ),
                DatingProfile(
                    name = "Camila Ramos",
                    age = 26,
                    occupation = "Bióloga Marina & Pianista",
                    bio = "Creo en la química que se siente al saludarse con un abrazo. Busco a alguien con quien debatir sobre ciencia o reírnos de tonterías sin juzgar.",
                    city = "Condesa",
                    distanceKm = 2,
                    verifiedHuman = true,
                    drawableResName = "profile_camila",
                    idealDateTitle = "Picnic al atardecer con quesos artesanales y vino",
                    idealDateDescription = "Una manta en el pasto, tabla con frutos secos y quesos, buena música acústica y una botella de vino tinto para compartir.",
                    idealDateLocation = "Parque México, área de lectura",
                    idealDateCategory = "Cena & Vinos",
                    interests = "Piano, Océanos, Gastronomía artesanal, Conciertos íntimos",
                    nonNegotiables = "Respeto por los seres vivos, Escucha activa, Sensibilidad",
                    relationshipGoal = "Relación seria y madura",
                    compatibilityScore = 90,
                    icebreakerPrompt = "Una regla que nunca rompo en una primera cita:",
                    icebreakerAnswer = "Ser 100% yo misma desde el primer minuto; los filtros solo atrasan lo inevitable."
                ),
                DatingProfile(
                    name = "Lucas Méndez",
                    age = 30,
                    occupation = "Librero & Cronista Urbano",
                    bio = "Colecciono libros usados y anécdotas de desconocidos. Me gustan los planes sencillos donde la conversación es el plato principal.",
                    city = "Coyoacán",
                    distanceKm = 6,
                    verifiedHuman = true,
                    drawableResName = "profile_lucas",
                    idealDateTitle = "Recorrido de librerías viejas y gelato de pistacho",
                    idealDateDescription = "Elegirnos mutuamente un libro al azar por menos de 10 dólares, hojearlo en un banco de la plaza y tomar un helado.",
                    idealDateLocation = "Plaza Hidalgo, Jardín Centenario",
                    idealDateCategory = "Arte & Cultura",
                    interests = "Literatura, Historia, Vinilos, Cafeterías antiguas",
                    nonNegotiables = "Curiosidad intelectual, Ternura, Autenticidad",
                    relationshipGoal = "Relación con complicidad profunda",
                    compatibilityScore = 94,
                    icebreakerPrompt = "El tema con el que puedo hablar durante horas:",
                    icebreakerAnswer = "Los pequeños secretos de la ciudad que la gente pasa por alto todos los días."
                )
            )

            dao.insertProfiles(sampleProfiles)

            // Seed user profile
            dao.insertOrUpdateUserProfile(
                UserProfile(
                    id = 1,
                    name = "Alma Valdés",
                    age = 26,
                    occupation = "Directora de Proyectos Creativos",
                    city = "Ciudad Central",
                    bio = "Creo en las miradas directas, el humor inteligente y las citas en la vida real sin pantallas de por medio.",
                    idealDate = "Café de especialidad en una librería oculta y caminar sin prisa.",
                    nonNegotiables = "Comunicación abierta, Puntualidad con cariño, Respeto mutuo",
                    isVerified = true,
                    antiGhostingPledgeSigned = true,
                    preferredDateCategory = "Café & Charla"
                )
            )

            // Match Camila initially to give instant access to chat and dates tab
            val camila = sampleProfiles[2]
            // We can match one profile to showcase the full flow immediately
            dao.matchProfile(3)
            dao.insertMessage(
                ChatMessage(
                    profileId = 3,
                    senderName = camila.name,
                    messageText = "¡Hola Alma! Me encantó que tengamos la misma visión sobre las citas reales. ¿Qué opinas de hacer realidad el picnic en Parque México este sábado?",
                    isFromUser = false,
                    isDateProposal = true
                )
            )

            // Seed an upcoming Real Date
            dao.insertDate(
                RealDate(
                    profileId = 3,
                    profileName = "Camila Ramos",
                    profileDrawable = "profile_camila",
                    title = "Picnic con quesos y vino acústico",
                    location = "Parque México, área de lectura",
                    dateTimeString = "Sábado 3 de Octubre - 18:00",
                    category = "Cena & Vinos",
                    status = "CONFIRMADA",
                    notes = "Manta azul llevada por Camila. Quedamos en la fuente de los patos a las 18:00.",
                    isSafetyProtocolActive = true
                )
            )
        }
    }
}
