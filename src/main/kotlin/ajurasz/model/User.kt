package ajurasz.model

data class User(val email: String, val password: String) {
    override fun toString() = "User(email=$email)"
}