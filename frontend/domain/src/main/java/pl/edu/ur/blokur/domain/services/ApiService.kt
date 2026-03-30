package pl.edu.ur.blokur.domain.services

//old project

//interface ApiService {
//    @POST("/api/auth/login")
//    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
//}

//object RetrofitClient {
//    private const val BASE_URL = "http://10.200.3.15:8080" // Emulator connection to localhost
//
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .build()
//
//    val apiService: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//}

//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
//
//class UserPreferences(private val context: Context) {
//
//    companion object {
//        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
//        private val USER_ROLE = stringPreferencesKey("user_role")
//    }
//
//    val authToken: Flow<String?>
//        get() = context.dataStore.data.map { preferences ->
//            preferences[JWT_TOKEN]
//        }
//
//    val userRole: Flow<String?>
//        get() = context.dataStore.data.map { preferences ->
//            preferences[USER_ROLE]
//        }
//
//    suspend fun saveAuthData(token: String, role: String) {
//        context.dataStore.edit { preferences ->
//            preferences[JWT_TOKEN] = token
//            preferences[USER_ROLE] = role
//        }
//    }
//
//    suspend fun clearAuthData() {
//        context.dataStore.edit { preferences ->
//            preferences.remove(JWT_TOKEN)
//            preferences.remove(USER_ROLE)
//        }
//    }
//}

//data class LoginRequest(
//    val username: String = "",
//    val password: String = ""
//)
//
//data class AuthResponse(
//    val token: String,
//    val role: String
//)

//class AuthRepository(
//    private val api: ApiService,
//    private val userPrefs: UserPreferences
//) {
//    suspend fun login(loginRequest: LoginRequest): Result<Unit> {
//        return try {
//            val response = api.login(loginRequest)
//            if (response.isSuccessful) {
//                val body = response.body()
//                if (body != null) {
//                    userPrefs.saveAuthData(body.token, body.role)
//                    Result.success(Unit)
//                } else {
//                    Result.failure(Exception("Brak danych uwierzytelniających w odpowiedzi"))
//                }
//            } else {
//                Result.failure(Exception("Błędny email lub hasło"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun logout() {
//        userPrefs.clearAuthData()
//    }
//}