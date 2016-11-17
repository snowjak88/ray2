# rays2

`rays2` is a backtracing raytracer with support for:

 - simple geometrical primitives: sphere, cube, cylinder, plane
 - 3 CSG operations: union, intersection, minus
 - lighting supplied by direct-diffuse (Lambertian), direct-specular (Phong), and ambient terms with support for refraction and reflection (using Schlick's approximation to the Fresnel equation)

There is rudimentary support for file-based world/scene description -- at this point, purely static (i.e., nothing in the way of iteration, named-definition-and-replication).

Antialiasing is implemented (albeit without any prior knowledge of signal-processing theory).

------

After three months' development, certain insufficiencies in the basic architecture, and mistakes made in initial design, have been causing a great deal of growing pain for new features. This, together with a growing realization that I lack understanding of a great deal of the basic theory involved in, e.g., signal processing, necessitates a basic re-assessment and redesign.

Therefore, development on `rays2` is suspended pending further research and self-education, followed by commencement on a new version of `rays`. Stay tuned for **`rays3`** (coming to a theater near you)!
