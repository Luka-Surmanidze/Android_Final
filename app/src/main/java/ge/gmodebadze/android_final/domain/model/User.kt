package ge.gmodebadze.android_final.domain.model

data class User(
    val uid: String,
    val nickname: String,
    val profession: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)