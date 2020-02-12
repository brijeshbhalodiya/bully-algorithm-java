public enum ResponseType {
    ALIVE, //response of IS_ALIVE request
    ELECTION_OK, //response of ELECTION request
    ACK, //response of JOIN request
    MEMBER_UPDATED //response of member_joined
}
