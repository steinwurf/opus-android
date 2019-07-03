// Copyright (c) 2017 Steinwurf ApS
// All Rights Reserved
//
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
// The copyright notice above does not evidence any
// actual or intended publication of such source code.

#pragma once

#include <system_error>

#include <jni.h>

#include "utils.hpp"

namespace jutils
{
class on_error_listener
{
public:

    on_error_listener(jobject listener)
    {
        auto env = get_jni_env();
        m_jlistener = env->NewGlobalRef(listener);
        auto clazz = env->GetObjectClass(m_jlistener);
        m_jmethod = get_method(env, clazz, "onError", "(Ljava/lang/String;)V");
    }

    ~on_error_listener()
    {
        auto env = get_jni_env();
        env->DeleteGlobalRef(m_jlistener);
    }

    void handle_error(const std::error_code& error)
    {
        auto env = get_jni_env();
        auto error_message = string_to_java_string(env, error.message());
        env->CallVoidMethod(m_jlistener, m_jmethod, error_message);
        env->DeleteLocalRef(error_message);
    }

private:

    jobject m_jlistener;
    jmethodID m_jmethod;
};
}
