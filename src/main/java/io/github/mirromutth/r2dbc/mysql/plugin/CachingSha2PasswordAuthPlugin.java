/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mirromutth.r2dbc.mysql.plugin;

import io.github.mirromutth.r2dbc.mysql.constant.AuthType;
import io.github.mirromutth.r2dbc.mysql.util.EmptyArrays;
import reactor.util.annotation.Nullable;

import java.security.MessageDigest;

import static io.github.mirromutth.r2dbc.mysql.util.AssertUtils.requireNonNull;

/**
 * MySQL Authentication Plugin for "caching_sha2_password"
 */
public final class CachingSha2PasswordAuthPlugin extends AbstractAuthPlugin {

    private static final CachingSha2PasswordAuthPlugin INSTANCE = new CachingSha2PasswordAuthPlugin();

    public static CachingSha2PasswordAuthPlugin getInstance() {
        return INSTANCE;
    }

    private CachingSha2PasswordAuthPlugin() {
    }

    @Override
    public AuthType getType() {
        return AuthType.CACHING_SHA2_PASSWORD;
    }

    /**
     * SHA256(password) all bytes xor SHA256( SHA256(SHA256(password)) + "random data from MySQL server" )
     *
     * @param password user password
     * @param scramble random data from MySQL server
     * @return encrypted authentication if password is not null, otherwise empty byte array.
     */
    @Override
    public byte[] encrypt(@Nullable byte[] password, byte[] scramble) {
        if (password == null) {
            return EmptyArrays.EMPTY_BYTES;
        }

        requireNonNull(scramble, "scramble must not be null");

        MessageDigest digest = loadDigest("SHA-256");
        byte[] oneRound = finalDigests(digest, password);
        byte[] twoRounds = finalDigests(digest, oneRound);
        byte[] result = finalDigests(digest, twoRounds, scramble);

        return allBytesXor(oneRound, result);
    }
}
