plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.glide)
    ksp(libs.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

}

data class User(
    val login: String,
    val avatar_url: String,
    val html_url: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val login: String,
    val avatar_url: String,
    val html_url: String
)

interface GitHubApi {
    @GET("users")
    suspend fun getUsers(): List<User>
}

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GitHubApi by lazy {
        retrofit.create(GitHubApi::class.java)
    }

}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
}

@Database(entities = [UserEntity::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = Room.databaseBuilder(
        application,
        UserDatabase::class.java, "user_database"
    ).build().userDao()

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> get() = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            // Проверка наличия интернет-соединения (можно использовать библиотеку для проверки)
            val userList = RetrofitInstance.api.getUsers()
            userList.forEach { user ->
                userDao.insertUser(UserEntity(user.login, user.avatar_url, user.html_url))
            }
            _users.postValue(userDao.getAllUsers())
        }
    }
}

@Composable
fun UserListScreen(viewModel: UserViewModel, navController: NavController) {
    val users by viewModel.users.observeAsState(emptyList())

    LazyColumn {
        items(users) { user ->
            Card(modifier = Modifier.padding(8.dp).clickable {
                navController.navigate("userDetail/${user.login}")
            }) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = rememberImagePainter(user.avatar_url),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )
                    Text(user.login, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun UserDetailScreen(userLogin: String, viewModel: UserViewModel) {
    // Здесь можно сделать дополнительный запрос, если необходимо
    Text("Details for $userLogin")
}
