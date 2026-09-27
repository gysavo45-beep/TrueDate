package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.RealDate
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DatingDao {
    @Query("SELECT * FROM dating_profiles WHERE isPassed = 0 ORDER BY compatibilityScore DESC")
    fun getFeedProfiles(): Flow<List<DatingProfile>>

    @Query("SELECT * FROM dating_profiles WHERE isMatched = 1 ORDER BY id DESC")
    fun getMatchedProfiles(): Flow<List<DatingProfile>>

    @Query("SELECT * FROM dating_profiles WHERE id = :id")
    fun getProfileById(id: Long): Flow<DatingProfile?>

    @Query("SELECT COUNT(*) FROM dating_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<DatingProfile>)

    @Update
    suspend fun updateProfile(profile: DatingProfile)

    @Query("UPDATE dating_profiles SET isLiked = 1, isMatched = 1 WHERE id = :id")
    suspend fun matchProfile(id: Long)

    @Query("UPDATE dating_profiles SET isPassed = 1 WHERE id = :id")
    suspend fun passProfile(id: Long)

    @Query("UPDATE dating_profiles SET isPassed = 0, isLiked = 0, isMatched = 0")
    suspend fun resetAllProfiles()

    // Real Dates
    @Query("SELECT * FROM real_dates ORDER BY createdTimestamp DESC")
    fun getAllDates(): Flow<List<RealDate>>

    @Query("SELECT * FROM real_dates WHERE profileId = :profileId ORDER BY createdTimestamp DESC")
    fun getDatesForProfile(profileId: Long): Flow<List<RealDate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDate(date: RealDate): Long

    @Update
    suspend fun updateDate(date: RealDate)

    @Query("DELETE FROM real_dates WHERE id = :id")
    suspend fun deleteDate(id: Long)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE profileId = :profileId ORDER BY timestamp ASC")
    fun getMessagesForProfile(profileId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfile)
}
