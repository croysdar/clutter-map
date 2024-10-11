// A tiny wrapper around fetch(), borrowed from
// https://kentcdodds.com/blog/replace-axios-with-a-simple-custom-fetch-wrapper

import { getCsrfTokenFromCookies } from "@/utils/utils"

interface ClientResponse<T> {
    status: number
    data: T
    headers: Headers
    url: string
}

export async function client<T>(
    endpoint: string,
    { body, ...customConfig }: Partial<RequestInit> = {},
): Promise<ClientResponse<T>> {
    const headers = { 'Content-Type': 'application/json' }

    const config: RequestInit = {
        method: body ? 'POST' : 'GET',
        ...customConfig,
        headers: {
            ...headers,
            'X-CSRF-Token': getCsrfTokenFromCookies() || '', // Automatically include CSRF token

            ...customConfig.headers,
        },
    }

    if (body) {
        config.body = typeof body === 'string' ? body : JSON.stringify(body)
    }

    try {
        const response = await window.fetch(endpoint, config)

        // Check if the response status is 204 (No Content)
        if (response.status === 204) {
            return {
                status: response.status,
                data: null as any,
                headers: response.headers,
                url: response.url,
            };
        }

        // Attempt to parse JSON only if the response has a JSON content type
        const contentType = response.headers.get('content-type');
        const isJson = contentType && contentType.includes('application/json');
        const data = isJson ? await response.json() : null;

        if (!response.ok) {
            throw new Error(
                JSON.stringify({ 
                    status: response.status, 
                    message: data?.message || 'Unknown Error' 
                })
            )
        }

        // Return a result object similar to Axios
        return {
            status: response.status,
            data,
            headers: response.headers,
            url: response.url,
        }
    } catch (err: any) {
        // Handle fetch error
        return Promise.reject(err.message ? err.message : { status: 500, message: 'Unknown error' })
    }
}

client.get = function <T>(endpoint: string, customConfig: Partial<RequestInit> = {}) {
    return client<T>(endpoint, { ...customConfig, method: 'GET' })
}

client.post = function <T>(endpoint: string, body: any, customConfig: Partial<RequestInit> = {}) {
    return client<T>(endpoint, { ...customConfig, body })
}
