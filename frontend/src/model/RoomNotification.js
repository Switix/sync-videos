import RoomNotificationType from '../constants/RoomNotificationTypes';

class RoomNotification {
    constructor(type = '', message = '', issuer = '') {
        if (type && !Object.values(RoomNotificationType).includes(type)) {
            throw new Error(`Invalid type: ${type}`);
        }
        this.type = type;
        this.message = message;
        this.issuer = issuer;
    }

    setType(type) {
        if (type && !Object.values(RoomNotificationType).includes(type)) {
            throw new Error(`Invalid type: ${type}`);
        }
        this.type = type;
    }

    setMessage(message) {
        this.message = message;
    }

    setIssuer(issuer) {
        this.issuer = issuer;
    }

    toString() {
        return `${this.type} [${this.issuer.username}]: ${this.message}`;
    }

}
export default RoomNotification;