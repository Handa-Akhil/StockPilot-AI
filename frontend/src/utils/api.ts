const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

interface RequestOptions extends RequestInit {
  headers?: Record<string, string>;
}

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
}

export const getTokens = () => {
  if (typeof window === 'undefined') return { token: null, refreshToken: null };
  return {
    token: localStorage.getItem('sp_access_token'),
    refreshToken: localStorage.getItem('sp_refresh_token'),
  };
};

export const setTokens = (token: string, refreshToken: string) => {
  localStorage.setItem('sp_access_token', token);
  localStorage.setItem('sp_refresh_token', refreshToken);
};

export const clearTokens = () => {
  localStorage.removeItem('sp_access_token');
  localStorage.removeItem('sp_refresh_token');
};

async function handleTokenRefresh(): Promise<string | null> {
  const { refreshToken } = getTokens();
  if (!refreshToken) return null;

  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token: refreshToken }),
    });

    if (!res.ok) {
      throw new Error('Refresh token invalid or expired');
    }

    const data = await res.json();
    setTokens(data.accessToken, data.refreshToken);
    return data.accessToken;
  } catch (error) {
    clearTokens();
    if (typeof window !== 'undefined') {
      window.location.href = '/login?expired=true';
    }
    return null;
  }
}

export async function apiFetch(endpoint: string, options: RequestOptions = {}): Promise<any> {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
  
  // Set headers
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Attach access token if present
  const { token } = getTokens();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const finalOptions: RequestInit = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, finalOptions);

    if (response.status === 401 && !endpoint.includes('/api/auth/login') && !endpoint.includes('/api/auth/refresh')) {
      // Access token might be expired, trigger token refresh
      if (!isRefreshing) {
        isRefreshing = true;
        handleTokenRefresh().then((newToken) => {
          isRefreshing = false;
          if (newToken) {
            onRefreshed(newToken);
          }
        });
      }

      // Return a promise that resolves when the token refresh is complete
      return new Promise((resolve, reject) => {
        subscribeTokenRefresh((newToken) => {
          // Update authorization header
          const newHeaders = { ...headers, Authorization: `Bearer ${newToken}` };
          fetch(url, { ...finalOptions, headers: newHeaders })
            .then((res) => {
              if (!res.ok) {
                return res.json().then(reject);
              }
              return res.json().then(resolve);
            })
            .catch(reject);
        });
      });
    }

    if (!response.ok) {
      const errData = await response.json().catch(() => ({}));
      throw new Error(errData.message || `HTTP error! status: ${response.status}`);
    }

    // Handles response formats (mostly JSON)
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    
    return await response.text();
  } catch (error) {
    throw error;
  }
}
