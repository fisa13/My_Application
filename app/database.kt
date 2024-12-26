import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

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

