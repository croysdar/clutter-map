if (typeof DOMException === 'undefined') {
    global.DOMException = class DOMException extends Error {
        constructor(message, name) {
            super(message);
            this.name = name || 'DOMException';
        }
    };
}

if (typeof structuredClone === 'undefined') {
    global.structuredClone = (obj) => JSON.parse(JSON.stringify(obj));
}

require('jest-localstorage-mock');
require('fake-indexeddb/auto');
