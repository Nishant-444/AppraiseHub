/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  }
  // ,
  // devIndicators: {
  //   buildActivity: false,
  // }
}

export default nextConfig
