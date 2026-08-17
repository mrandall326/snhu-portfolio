# CRUD_Python_Module.py
#
# Purpose : A portable, reusable CRUD access layer that connects a Python
#           client (a Jupyter notebook now, a Dash dashboard in Project Two)
#           to the Austin Animal Center data in MongoDB. Keeping the database
#           code in its own importable module means the interface never talks
#           to MongoDB directly.
# Scope   : All four CRUD operations - Create, Read, Update, and Delete.
# Target  : Database 'aac', collection 'animals'.
# Author  : Matthew Randall

import logging
import urllib.parse

from pymongo import MongoClient
# Catching PyMongoError rather than Exception means a bug in my own calling
# code doesn't get reported as a database failure.
from pymongo.errors import PyMongoError

# Logging instead of print, so whatever imports this module decides where the
# messages go. In Project Two that's a server log, not a notebook cell.
logger = logging.getLogger(__name__)


class AnimalShelter:
    """CRUD operations on the 'animals' collection in the 'aac' database.

    Instantiate once with valid credentials and reuse it. The connection is
    opened a single time rather than on every call.

    Attributes:
        client: the authenticated MongoClient connection.
        database: handle to the 'aac' database.
        collection: handle to the 'animals' collection, which every method
            below works through.
    """

    # These are the same for every instance, so I set them here instead of
    # rebuilding them inside __init__ each time.
    HOST = 'localhost'
    PORT = 27017
    DATABASE_NAME = 'aac'
    COLLECTION_NAME = 'animals'

    def __init__(self, username, password):
        """Open an authenticated connection to the aac database.

        Args:
            username (str): the account to authenticate with, e.g. 'aacuser'.
            password (str): that account's password, passed in raw. It gets
                percent-encoded here, so don't encode it beforehand.

        Raises:
            ValueError: if either credential is missing.
        """
        # Taking the credentials as arguments keeps them out of this file and
        # lets any account use the module without editing it.
        if not username or not password:
            raise ValueError('A username and password are both required')

        # '@', ':' and '/' all mean something inside a URI, so a password
        # containing one would break the connection string if I dropped it in
        # unencoded.
        safe_user = urllib.parse.quote_plus(username)
        safe_password = urllib.parse.quote_plus(password)

        # aacuser is defined in the admin database but holds readWrite on aac.
        # Mongo authenticates against whichever database owns the user, so
        # without authSource=admin the login fails. This is what tripped me up
        # in Module Three until db.getUsers() showed me where the user lived.
        uri = (
            f'mongodb://{safe_user}:{safe_password}'
            f'@{self.HOST}:{self.PORT}/?authSource=admin'
        )

        self.client = MongoClient(uri)
        self.database = self.client[self.DATABASE_NAME]
        self.collection = self.database[self.COLLECTION_NAME]

    def create(self, data):
        """Insert one document into the animals collection.

        Args:
            data (dict): the document to insert, for example
                {'name': 'Rex', 'animal_type': 'Dog'}.

        Returns:
            bool: True if MongoDB acknowledged the write, False on a database
                error.

        Raises:
            ValueError: if data is None.
        """
        # Calling this with nothing is a mistake in the calling code, not a
        # database problem, so I raise instead of quietly returning False.
        if data is None:
            raise ValueError('Nothing to save, because data parameter is '
                             'empty')

        try:
            result = self.collection.insert_one(data)
            # acknowledged is True once the server confirms the write, which
            # is the success signal this method is supposed to return.
            return result.acknowledged
        except PyMongoError as error:
            logger.error('create() failed: %s', error)
            return False

    def read(self, query):
        """Query documents from the animals collection.

        Uses find() rather than find_one() so every match comes back, not
        just the first one.

        Args:
            query (dict): the lookup pair(s) for find(). An empty dict
                matches every document.

        Returns:
            list: the matching documents, or an empty list on a database
                error.

        Raises:
            ValueError: if query is None.
        """
        if query is None:
            raise ValueError('Nothing to query, because query parameter is '
                             'empty')

        try:
            # find() hands back a cursor, not documents. Wrapping it in list()
            # walks the cursor once so the caller gets something reusable.
            cursor = self.collection.find(query)
            return list(cursor)
        except PyMongoError as error:
            logger.error('read() failed: %s', error)
            return []

    def update(self, query, update_data):
        """Change existing documents in the animals collection.

        Uses update_many() so every match is changed, which lines up with the
        "changes document(s)" wording in the spec and with how read() works.

        Args:
            query (dict): the lookup pair(s) for the documents to change.
            update_data (dict): the change to apply. Either form works - pass
                it with a MongoDB operator, e.g.
                {'$set': {'outcome_type': 'Transfer'}}, or pass the plain
                key/value pairs, e.g. {'outcome_type': 'Transfer'}, and this
                method wraps them in $set. Accepting the operator form means
                $inc, $unset and the rest still work.

        Returns:
            int: how many documents were modified, or 0 on a database error.

        Raises:
            ValueError: if either argument is None.
        """
        if query is None or update_data is None:
            raise ValueError('Nothing to update, because the query or '
                             'update_data parameter is empty')

        # update_many() needs an update operator. If the caller passed plain
        # key/value pairs instead, wrap them in $set so the call still works.
        # A dict is either all operators or all field names, so checking one
        # key is enough.
        if update_data and not any(key.startswith('$') for key in update_data):
            update_data = {'$set': update_data}

        try:
            result = self.collection.update_many(query, update_data)
            # modified_count, not matched_count. A document that already holds
            # the new value gets matched but nothing about it changes.
            return result.modified_count
        except PyMongoError as error:
            logger.error('update() failed: %s', error)
            return 0

    def delete(self, query):
        """Remove documents from the animals collection.

        Uses delete_many() so every match is removed, matching the
        "removes document(s)" wording in the spec.

        Args:
            query (dict): the lookup pair(s) for the documents to remove. An
                empty dict would match the whole collection, so always pass a
                specific filter.

        Returns:
            int: how many documents were removed, or 0 on a database error.

        Raises:
            ValueError: if query is None.
        """
        # A delete can't be undone, so I reject a missing filter here rather
        # than let it through as "match everything".
        if query is None:
            raise ValueError('Nothing to delete, because query parameter is '
                             'empty')

        try:
            result = self.collection.delete_many(query)
            return result.deleted_count
        except PyMongoError as error:
            logger.error('delete() failed: %s', error)
            return 0

    def close(self):
        """Close the MongoDB connection.

        Mostly for scripts and notebooks that create an instance and then
        finish. The Project Two dashboard keeps one instance alive the whole
        time it runs.
        """
        self.client.close()
